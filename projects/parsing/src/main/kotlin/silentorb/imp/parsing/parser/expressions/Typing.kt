package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun getLiteralRuneType(rune: Rune): TypeHash? =
    when (rune) {
      Rune.literalInteger -> intSignature.hashCode()
      Rune.literalFloat -> floatSignature.hashCode()
      else -> null
    }

fun resolveLiteralTypes(tokens: Tokens, tokenNodes: Map<TokenIndex, PathKey>): Map<PathKey, TypeHash> {
  return tokenNodes.entries
      .associate { (tokenIndex, pathKey) ->
        val token = tokens[tokenIndex]
        val type = getLiteralRuneType(token.rune)!!
        Pair(pathKey, type)
      }
}
