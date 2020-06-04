package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.lexer.Rune

fun parseType(returnMode: ParsingMode): TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier -> returnMode to skip
    else -> null to addError(TextId.expectedColon)
  }
}
