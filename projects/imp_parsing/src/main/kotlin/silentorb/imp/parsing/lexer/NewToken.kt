package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.Range
import silentorb.imp.parsing.general.Token

fun newToken(mode: LexicalMode, range: Range, buffer: LexicalBuffer): Token? {
  val rune = getRune(mode)
  return if (rune == null)
    null
  else
    Token(
        rune,
        text = buffer,
        range = range
    )
}
