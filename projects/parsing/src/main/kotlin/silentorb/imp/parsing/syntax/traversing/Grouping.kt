package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseGroupStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArgumentStart) + startGroup
        isParenthesesClose(token) -> descend
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseGroupArgumentStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonArgument(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArgumentStart) + startArgument + startGroup
        isParenthesesClose(token) -> descend
        isDot(token) -> startPipingRoot
        else -> parsingError(TextId.invalidToken)
      }
}

val parseGroupFollowingArgument: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: parseExpressionFollowingArgument(ParsingMode.groupArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArgumentStart) + startGroup
        isParenthesesClose(token) -> descend
        isAssignment(token) -> closeArgumentName
        isEndOfFile(token) -> ParsingStep(closeArgumentValue + fold, ParsingMode.body)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseGroupRootNamedArgumentValue: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonNamedArgumentValue(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.groupArgumentStart) + startGroup
        isParenthesesClose(token) -> descend
        isNewline(token) -> skipStep
        isEndOfFile(token) -> parsingError(TextId.missingExpression)
        else -> parsingError(TextId.invalidToken)
      }
}
