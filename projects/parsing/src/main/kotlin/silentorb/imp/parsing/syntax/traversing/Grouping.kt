package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*
/*
val parseGroupStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) ->startGroup
        isParenthesesClose(token) -> closeGroup
        isDot(token) -> addError(TextId.missingLefthandExpression)
        else -> addError(TextId.invalidToken)
      }
}

val parseGroupArgumentStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingClosingParenthesis) + nextDefinition }
      ?: parseExpressionCommonArgument(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup +startArgument + startGroup
        isParenthesesClose(token) -> closeGroup
        isDot(token) -> startPipingGroup
        else -> addError(TextId.invalidToken)
      }
}

val parseGroupFollowingArgument: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: onMatch(isAssignment(token)) { closeArgumentName }
      ?: parseExpressionFollowingArgument(ParsingMode.groupArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isParenthesesOpen(token) ->startGroup
        isParenthesesClose(token) -> closeGroup
        isEndOfFile(token) -> closeArgumentValue + fold + goto(ParsingMode.body)
        else -> addError(TextId.invalidToken)
      }
}

val parseGroupRootNamedArgumentValue: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonNamedArgumentValue(ParsingMode.groupArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) ->startGroup
        isParenthesesClose(token) -> closeGroup
        isNewline(token) -> skip
        isEndOfFile(token) -> addError(TextId.missingExpression)
        else -> addError(TextId.invalidToken)
      }
}
*/
