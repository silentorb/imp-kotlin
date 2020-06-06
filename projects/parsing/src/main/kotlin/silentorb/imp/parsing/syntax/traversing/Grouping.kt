package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseSubExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startGroup
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    isDot(token) -> parsingError(TextId.missingLefthandExpression)
    else -> parseExpressionCommonStart(ParsingMode.groupArguments)(token)
  }
}

val parseSubExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startArgument + startGroup
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    isDot(token) -> startPipingRoot
    else -> parseExpressionCommonArgument(ParsingMode.groupArguments)(token)
  }
}
