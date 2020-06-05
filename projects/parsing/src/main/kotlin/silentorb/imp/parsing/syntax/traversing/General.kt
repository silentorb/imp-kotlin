package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun parseType(returnMode: ParsingMode): TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(skip, returnMode)
    else -> parsingError(TextId.expectedColon)
  }
}
