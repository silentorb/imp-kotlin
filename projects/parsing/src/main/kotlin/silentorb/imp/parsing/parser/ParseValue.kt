package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

data class ResolvedLiteral(
    val type: PathKey,
    val value: Any
)

fun parseTokenLiteral(type: PathKey, value: String): Any? =
    when (type) {
      floatType.key -> value.toFloat()
      intType.key -> value.toInt()
      else -> null
    }

fun parseTokenLiteral(rune: Rune, value: String): Any? =
    when (rune) {
      Rune.literalFloat -> value.toFloat()
      Rune.literalInteger -> value.toInt()
      else -> null
    }

fun parseTokenLiteral(token: Token) =
    parseTokenLiteral(token.rune, token.value)
