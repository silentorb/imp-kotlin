package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.*

fun mapExpressionTokensToNodes(root: PathKey, tokens: Tokens): ParsingResponse<IntermediateExpression> {
  val path = pathKeyToString(root)
  val groupGraph = newGroupingGraph(groupTokens(tokens))
  val tokenGraph = arrangePiping(tokens, groupGraph)
  val namedArguments = tokenGraph.parents
      .map { (_, children) -> getNamedArguments(tokens, children) }
      .reduce { a, b -> a.plus(b) }
  val parents = collapseNamedArgumentClauses(namedArguments.keys, tokenGraph.parents)
  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val literalTokenKeys = literalTokenNodes(path, tokens, indexedTokens)
  val nodeReferences = indexedTokens - literalTokenKeys.keys
  val tokenNodes = nodeReferences
      .groupBy { tokens[it].value }
      .flatMap { (name, tokenIndices) ->
        tokenIndices.mapIndexed { index, tokenIndex ->
          Pair(tokenIndex, PathKey(path, "$name${index + 1}"))
        }
      }
      .associate { it }
      .plus(literalTokenKeys)

  val nodeMap = tokenNodes.entries
      .associate { (tokenIndex, pathKey) ->
        Pair(pathKey, tokens[tokenIndex].fileRange)
      }

  val literalTypes = resolveLiteralTypes(tokens, literalTokenKeys)

  val pipingErrors = validatePiping(tokens, groupGraph)
  val pathKeyParents = parents.entries
      .mapNotNull { (key, value) ->
        val parent = tokenNodes[key]
        if (parent != null)
          Pair(parent, value.map { tokenNodes[it]!! })
        else
          null
      }
      .associate { it }

  return ParsingResponse(
      IntermediateExpression(
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = pathKeyParents,
          references = nodeReferences.groupBy { tokens[it].value }.mapValues { it.value.map { tokenNodes[it]!! }.toSet() },
          stages = tokenGraph.stages.map { stage -> stage.mapNotNull { tokenNodes[it] } },
          namedArguments = namedArguments.mapKeys { (tokenIndex, _) -> tokenNodes[tokenIndex]!! },
          values = resolveLiterals(tokens, indexedTokens, tokenNodes)
      ),
      pipingErrors
  )
}

fun parseExpression(context: Context, largerContext: Context, intermediate: IntermediateExpression): ParsingResponse<Dungeon> {
  val (
      literalTypes,
      namedArguments,
      nodeMap,
      parents,
      references,
      stages,
      values
  ) = intermediate

  val referencePairs = references
      .flatMap { (typeName, referenceNodes) ->
        val type = getSymbolType(context, typeName)
            ?: unknownType.hash
        val targetKey = resolveReference(context, typeName) ?: unknownType.key
        referenceNodes.map { Pair(it, Pair(type, targetKey)) }
      }
      .associate { it }

  val referenceTypes = referencePairs
      .mapValues { it.value.first }

  val referenceKeys = referencePairs
      .mapValues { it.value.second }

  val initialTypes = literalTypes + referenceTypes
  val (signatureOptions, implementationTypes, reducedTypes, typings) =
      resolveFunctionSignatures(
          largerContext,
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
  val nodeTypes = initialTypes + reducedTypes
  val nonNullaryFunctions = parents.filter { it.value.any() }
  val typeResolutionErrors = validateFunctionTypes(nodeMap.keys, initialTypes, nodeMap)
  val signatureErrors = validateSignatures(largerContext, nodeTypes, nonNullaryFunctions, signatureOptions, nodeMap)// +
  val errors = signatureErrors + typeResolutionErrors

  val dungeon = emptyDungeon.copy(
      graph = newNamespace().copy(
          connections = connections + referenceKeys.entries.associate { Input(it.key, defaultParameter) to it.value },
          implementationTypes = implementationTypes,
          returnTypes = nodeTypes,
          typings = typings,
          values = values
      ),
      nodeMap = nodeMap
  )
  return ParsingResponse(dungeon, errors)
}
