package silentorb.imp.parsing.parser

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun parseValue(rune: Rune, value: String): Any? =
    when (rune) {
      Rune.literalFloat -> value.toFloat()
      Rune.literalInteger -> value.toInt()
      else -> null
    }

fun parseTokenValue(token: Token) =
    parseValue(token.rune, token.value)
