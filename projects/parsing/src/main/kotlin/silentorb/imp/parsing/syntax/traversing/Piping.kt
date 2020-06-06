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
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startPipingGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    isDot(token) -> parsingError(TextId.missingLefthandExpression)
    else -> parseExpressionElementsPiping(ParsingMode.expressionRootArguments)(token)
  }
}

val parseGroupedPipingStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startPipingGroup
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    isDot(token) -> parsingError(TextId.missingLefthandExpression)
    else -> parseExpressionElementsPiping(ParsingMode.groupArguments)(token)
  }
}
