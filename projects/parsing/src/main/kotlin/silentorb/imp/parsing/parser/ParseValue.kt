package silentorb.imp.parsing.parser

import silentorb.imp.core.PathKey
import silentorb.imp.core.floatKey
import silentorb.imp.core.intKey
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

data class ResolvedLiteral(
    val type: PathKey,
    val value: Any
)

fun parseTokenLiteral(rune: Rune, value: String): ResolvedLiteral? =
    when (rune) {
      Rune.literalFloat -> ResolvedLiteral(floatKey, value.toFloat())
      Rune.literalInteger -> ResolvedLiteral(intKey, value.toInt())
      else -> null
    }

fun parseTokenLiteral(token: Token) =
    parseTokenLiteral(token.rune, token.value)
