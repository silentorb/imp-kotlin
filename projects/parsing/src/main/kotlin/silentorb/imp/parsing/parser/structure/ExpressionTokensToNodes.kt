package silentorb.imp.parsing.parser.structure

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.expressions.*
import silentorb.imp.parsing.parser.validatePiping

fun expressionTokensToNodes(root: PathKey, tokens: Tokens): ParsingResponse<IntermediateExpression> {
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
