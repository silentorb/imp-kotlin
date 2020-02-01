package silentorb.imp.parsing.parser

import silentorb.imp.core.PathKey
import silentorb.imp.core.floatKey
import silentorb.imp.core.intKey
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun parseValue(rune: Rune, value: String): Pair<PathKey, Any>? =
    when (rune) {
      Rune.literalFloat -> Pair(floatKey, value.toFloat())
      Rune.literalInteger -> Pair(intKey, value.toInt())
      else -> null
    }

fun parseTokenValue(token: Token) =
    parseValue(token.rune, token.value)
