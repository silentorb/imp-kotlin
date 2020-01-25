package silentorb.imp.parsing.parser

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun parseValue(rune: Rune, value: String): Any =
    when (rune) {
      Rune.literalFloat -> value.toFloat()
      Rune.literalInteger -> value.toInt()
      else -> throw Error("Unsupported value type $rune")
    }

fun parseValueToken(token: Token): Any {
  val value = token.text
  if (value == null)
    throw Error("Value cannot be null")

  return parseValue(token.rune, value)
}
