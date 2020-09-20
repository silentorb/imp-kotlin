package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.Burg
import silentorb.imp.parsing.syntax.BurgType

fun getLiteralRuneType(rune: Rune): TypeHash? =
    when (rune) {
      Rune.literalInteger -> intType.hash
      Rune.literalFloat -> floatType.hash
      else -> null
    }

fun getLiteralBurgType(type: BurgType): TypeHash? =
    when (type) {
      BurgType.literalInteger -> intType.hash
      BurgType.literalFloat -> floatType.hash
      BurgType.literalString -> stringType.hash
      else -> null
    }

fun resolveLiteralTypes(tokenNodes: Map<Burg, PathKey>): Map<PathKey, TypeHash> {
  return tokenNodes.entries
      .associate { (token, pathKey) ->
        val type = getLiteralBurgType(token.type)!!
        Pair(pathKey, type)
      }
}
