package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

fun parseExpressionElementsPiping(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      applyPiping(burgType, translator) + goto(mode)
    }

val parsePipingRootStart: TokenToParsingTransition = { token ->
  onMatch(isAnyDefinitionStart(token)) { addError(TextId.missingRighthandExpression) + nextDefinition(token) }
      ?: parseExpressionElementsPiping(ParsingMode.expressionArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup
        isParenthesesClose(token) -> tryCloseGroup
        isNewline(token) -> skip
        isDot(token) -> addError(TextId.missingLefthandExpression)
        else -> addError(TextId.invalidToken)
      }
}
