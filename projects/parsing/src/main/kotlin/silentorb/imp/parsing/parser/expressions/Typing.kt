package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun getRuneType(rune: Rune): PathKey? =
    when (rune) {
      Rune.literalInteger -> intKey
      Rune.literalFloat -> floatKey
      else -> null
    }

fun resolveFunctionTypes(context: Context, tokens: Tokens, indexes: List<TokenIndex>,
                         tokenNodes: Map<TokenIndex, Id>): Map<Id, PathKey> {
  return indexes
      .mapNotNull { tokenIndex ->
        val token = tokens[tokenIndex]
        val type = getFunctionReference(context)(token)
        if (type != null) {
          val id = tokenNodes[tokenIndex]!!
          Pair(id, type)
        } else
          null
      }
      .associate { it }
}

fun resolveLiteralTypes(tokens: Tokens, indexes: List<TokenIndex>,
                         tokenNodes: Map<TokenIndex, Id>): Map<Id, PathKey> {
  return indexes
      .mapNotNull { tokenIndex ->
        val token = tokens[tokenIndex]
        val type = getRuneType(token.rune)
        if (type != null) {
          val id = tokenNodes[tokenIndex]!!
          Pair(id, type)
        } else
          null
      }
      .associate { it }
}
