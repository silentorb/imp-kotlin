package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun parseExpressionElementsPiping(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      ParsingStep(
          applyPiping(burgType, translator)
          , mode)
    }

val parsePipingRootStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingRighthandExpression) + nextDefinition }
      ?: parseExpressionElementsPiping(ParsingMode.expressionRootArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentStart) + startPipingGroup
        isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
        isNewline(token) -> skipStep
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseGroupedPipingStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionElementsPiping(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArgumentStart) + startPipingGroup
        isParenthesesClose(token) -> closeGroup
        isNewline(token) -> skipStep
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        else -> parsingError(TextId.invalidToken)
      }
}
