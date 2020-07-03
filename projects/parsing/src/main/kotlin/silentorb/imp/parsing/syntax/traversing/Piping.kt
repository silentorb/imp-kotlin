package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun parseExpressionElementsPiping(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      applyPiping(burgType, translator) + goto(mode)
    }

val parsePipingRootStart: ContextualTokenToParsingTransition = { token, contextMode ->
  onMatch(isLet(token)) { addError(TextId.missingRighthandExpression) + nextDefinition }
      ?: parseExpressionElementsPiping(ParsingMode.expressionArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup
        isParenthesesClose(token) -> tryCloseGroup(contextMode)
        isNewline(token) -> skip
        isDot(token) -> addError(TextId.missingLefthandExpression)
        else -> addError(TextId.invalidToken)
      }
}
