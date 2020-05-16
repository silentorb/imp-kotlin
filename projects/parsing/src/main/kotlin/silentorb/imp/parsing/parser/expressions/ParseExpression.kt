package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.*

fun parseExpression(root: PathKey, context: Context, tokens: Tokens): PartitionedResponse<Dungeon> {
  val path = "${root.path}.${root.name}"
  val groupGraph = newGroupingGraph(groupTokens(tokens))
  val tokenGraph = arrangePiping(tokens, groupGraph)
  assert(tokenGraph.stages.last().size == 1)
  val namedArguments = tokenGraph.parents
      .map { (_, children) -> getNamedArguments(tokens, children) }
      .reduce { a, b -> a.plus(b) }
  val parents = collapseNamedArgumentClauses(namedArguments.keys, tokenGraph.parents)
  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val nodeReferences = resolveReferences(context, tokens, indexedTokens)
  val literalTokenKeys = literalTokenNodes(path, tokens, indexedTokens)
  val tokenNodes = indexedTokens
      .minus(literalTokenKeys.keys)
      .groupBy { tokens[it].value }
      .flatMap { (name, tokenIndices) ->
        tokenIndices.mapIndexed { index, tokenIndex ->
          Pair(tokenIndex, PathKey(path, "$name${index + 1}"))
        }
      }
      .associate { it }
      .plus(literalTokenKeys)

  val nodes = tokenNodes.values.toSet()
  val nonNodeReferenceTokens = indexedTokens.minus(nodeReferences.keys)
  val literalTypes = resolveLiteralTypes(tokens, nonNodeReferenceTokens, tokenNodes)
  val functionTypes = nodeReferences.mapKeys { tokenNodes[it.key]!! }
  val obviousTypes = literalTypes // + associateWithNotNull(nodeReferences.values) { resolveAlias(context, it) }

  val (signatureOptions, functionReturnTypes) = resolveFunctionSignatures(
      context,
      tokenGraph,
      parents,
      functionTypes,
      obviousTypes,
      tokenNodes,
      tokens,
      namedArguments
  )
  val types = obviousTypes.plus(functionReturnTypes)
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, tokenNodes, signatures)
  val values = resolveLiterals(tokens, indexedTokens, tokenNodes)
  val newNodes = nodes.minus(nodeReferences.values)
  val nodeMap = newNodes
      .associateWith { id ->
        val index = tokenNodes.entries.first { it.value == id }.key
        tokens[index].range
      }
  val rootTokenIndex = tokenGraph.stages.last().first()
  val rootNode = tokenNodes[rootTokenIndex]!!

  // TODO: Validate function types
//  val typeResolutionErrors = validateFunctionTypes(newNodes, functionTypes.plus(types), nodeMap)
  val signatureErrors = validateSignatures(signatureOptions, nodeMap) +
      validateMissingSignatures(functionTypes, signatures, nodeMap)
  val pipingErrors = validatePiping(tokens, groupGraph)
  val errors = signatureErrors + /*typeResolutionErrors +*/ pipingErrors

  val dungeon = Dungeon(
      graph = newNamespace().copy(
//          nodes = nodes,
          connections = connections,
          references = functionTypes + (root to rootNode),
          nodeTypes = literalTypes,
//          signatureMatches = signatures,
          values = values
      ),
      nodeMap = nodeMap,
      literalConstraints = mapOf()
  )
  return PartitionedResponse(dungeon, errors)
}
