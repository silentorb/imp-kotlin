package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.PathKey
import silentorb.imp.parsing.general.Tokens

fun nodeReferenceTokens(nodeReferences: NodeReferenceMap, indexes: List<TokenIndex>): Map<TokenIndex, PathKey> =
    indexes
        .filter { tokenIndex ->
          nodeReferences.containsKey(tokenIndex)
        }
        .associateWith { nodeReferences[it]!! }

fun literalTokenNodes(path: String, tokens: Tokens, indexedTokens: List<TokenIndex>): Map<TokenIndex, PathKey> {
  val literalNodes = indexedTokens.filter { getLiteralRuneType(tokens[it].rune) != null }
  return literalNodes
      .mapIndexed { index, tokenIndex -> Pair(tokenIndex, PathKey(path, "#literal${index + 1}")) }
      .associate { it }
}
