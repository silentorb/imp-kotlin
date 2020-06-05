package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseExpression: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> startSubExpression
    isIdentifier(token) -> ParsingStep(push(BurgType.reference, asString))
    isFloat(token) -> ParsingStep(push(BurgType.literalFloat, asFloat))
    isInteger(token) -> ParsingStep(push(BurgType.literalInteger, asInt))
    else -> parsingError(TextId.invalidToken)
  }
}

val parseSubExpression: TokenToParsingTransition = { token ->
  when {
    isParenthesesClose(token) -> ParsingStep(push(BurgType.expression, asMarker))
    else -> parseExpression(token)
  }
}

fun parseExpressionStart(mode: ParsingMode): TokenToParsingTransition = { token ->
  val intermediate = parseExpression(token)
  intermediate.copy(
      transition = push(BurgType.expression, asMarker) + intermediate.transition
  )
}
