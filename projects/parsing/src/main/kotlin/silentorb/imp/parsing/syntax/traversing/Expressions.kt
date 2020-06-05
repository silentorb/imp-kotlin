package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun expressionStep(returnMode: ParsingMode, burgType: BurgType, translator: ValueTranslator) =
    ParsingStep(push(BurgType.expression, asMarker) + push(burgType, translator) + fold, returnMode)

fun parseExpression(returnMode: ParsingMode): TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> expressionStep(returnMode, BurgType.reference, asString)
    isFloat(token) -> expressionStep(returnMode, BurgType.literalFloat, asFloat)
    isInteger(token) -> expressionStep(returnMode, BurgType.literalInteger, asInt)
    else -> parsingError(TextId.invalidToken)
  }
}
