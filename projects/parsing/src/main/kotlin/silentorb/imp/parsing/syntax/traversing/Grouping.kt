package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseSubExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.groupArguments)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startGroup
        isParenthesesClose(token) -> descend
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseSubExpressionArguments: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonArgument(ParsingMode.groupArguments)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startArgument + startGroup
        isParenthesesClose(token) -> descend
        isDot(token) -> startPipingRoot
        else -> parsingError(TextId.invalidToken)
      }
}
