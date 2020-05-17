package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.*

data class IntermediateExpression(
    val initialTypes: Map<PathKey, TypeHash>,
    val namedArguments: Map<PathKey, String>,
    val nodeMap: NodeMap,
    val parents: Map<PathKey, List<PathKey>>,
    val stages: List<List<PathKey>>,
    val values: Map<PathKey, Any>
)

fun mapTokensToNodes(root: PathKey, context: Context, tokens: Tokens): PartitionedResponse<IntermediateExpression> {
  val path = "${root.path}.${root.name}"
  val groupGraph = newGroupingGraph(groupTokens(tokens))
  val tokenGraph = arrangePiping(tokens, groupGraph)
  assert(tokenGraph.stages.last().size == 1)
  val namedArguments = tokenGraph.parents
      .map { (_, children) -> getNamedArguments(tokens, children) }
      .reduce { a, b -> a.plus(b) }
  val parents = collapseNamedArgumentClauses(namedArguments.keys, tokenGraph.parents)
  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val literalTokenKeys = literalTokenNodes(path, tokens, indexedTokens)
  val nodeReferences = resolveReferences(context, tokens, indexedTokens - literalTokenKeys.keys)
  val tokenNodes = nodeReferences
      .entries
      .groupBy { it.value.name }
      .flatMap { (name, tokenIndices) ->
        tokenIndices.mapIndexed { index, tokenIndex ->
          Pair(tokenIndex.key, PathKey(path, "$name${index + 1}"))
        }
      }
      .associate { it }
      .plus(literalTokenKeys)
  val nodeMap = tokenNodes.entries
      .associate { (tokenIndex, pathKey) ->
        Pair(pathKey, tokens[tokenIndex].range)
      }
      .plus(indexedTokens.minus(tokenNodes.keys)
          .mapIndexed { index, tokenIndex ->
            Pair(PathKey(path, "#unknown${index + 1}"), tokens[tokenIndex].range)
          })

  val literalTypes = resolveLiteralTypes(tokens, literalTokenKeys)
  val referenceTypes = tokenNodes
      .minus(literalTokenKeys.keys)
      .entries
      .associate { Pair(it.value, getSymbolType(context, tokens[it.key].value)) }
      .filter { it.value != null }.mapValues { it.value!! }

  val pipingErrors = validatePiping(tokens, tokenGraph)
  val pathKeyParents = parents.entries
      .mapNotNull { (key, value) ->
        val parent = tokenNodes[key]
        if (parent != null)
          Pair(parent, value.map { tokenNodes[it]!! })
        else
          null
      }
      .associate { it }

  return PartitionedResponse(
      IntermediateExpression(
          initialTypes = literalTypes + referenceTypes,
          nodeMap = nodeMap,
          parents = pathKeyParents,
          stages = tokenGraph.stages.map { stage -> stage.mapNotNull { tokenNodes[it] } },
          namedArguments = namedArguments.mapKeys { (tokenIndex, _) -> tokenNodes[tokenIndex]!! },
          values = resolveLiterals(tokens, indexedTokens, tokenNodes)
      ),
      pipingErrors
  )
}

fun parseExpression(root: PathKey, context: Context, tokens: Tokens): PartitionedResponse<Dungeon> {
  val (intermediate, tokenErrors) = mapTokensToNodes(root, context, tokens)
  val (
      initialTypes,
      namedArguments,
      nodeMap,
      parents,
      stages,
      values
  ) = intermediate

  val (signatureOptions, functionReturnTypes) = resolveFunctionSignatures(
      context,
      stages,
      parents,
      initialTypes,
      nodeMap,
      namedArguments
  )
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, signatures)
//  val rootNode = stages.last().first()
  val nonNullaryFunctions = parents.filter { it.value.any() }.keys
  // TODO: Validate function types and signatures
  val typeResolutionErrors = validateFunctionTypes(nodeMap.keys, initialTypes, nodeMap)
  val signatureErrors = validateSignatures(nonNullaryFunctions, signatureOptions, nodeMap)// +
//      validateMissingSignatures(functionTypes, signatures, nodeMap)
  val errors = signatureErrors + typeResolutionErrors + tokenErrors

  val dungeon = Dungeon(
      graph = newNamespace().copy(
//          nodes = nodes,
          connections = connections,
//          references = (root to rootNode),
          nodeTypes = initialTypes,
//          signatureMatches = signatures,
          values = values
      ),
      nodeMap = nodeMap,
      literalConstraints = mapOf()
  )
  return PartitionedResponse(dungeon, errors)
}
