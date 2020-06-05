package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun parseExpression(returnMode: ParsingMode): TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(pushChild + foldStack, returnMode, BurgType.reference)
    isFloat(token) -> ParsingStep(pushChild + foldStack, returnMode, BurgType.literalFloat)
    isInteger(token) -> ParsingStep(pushChild + foldStack, returnMode, BurgType.literalInteger)
    else -> parsingError(TextId.invalidToken)
  }
}
