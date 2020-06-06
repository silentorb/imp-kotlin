package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseExpressionCommonStart: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(push(BurgType.reference, asString) + pushEmpty)
    isFloat(token) -> ParsingStep(push(BurgType.literalFloat, asFloat))
    isInteger(token) -> ParsingStep(push(BurgType.literalInteger, asInt))
    isNewline(token) -> skipStep
    else -> parsingError(TextId.invalidToken)
  }
}

val parseExpressionCommonArgument: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(append(BurgType.reference, asString))
    isFloat(token) -> ParsingStep(append(BurgType.literalFloat, asFloat))
    isInteger(token) -> ParsingStep(append(BurgType.literalInteger, asInt))
    isNewline(token) -> skipStep
    else -> parsingError(TextId.invalidToken)
  }
}

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startSubExpression
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    else -> parseExpressionCommonStart(token)
  }
}

val parseRootExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startSubExpression
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    isEndOfFile(token) -> ParsingStep(fold)
    else -> parseExpressionCommonArgument(token)
  }
}

val parseSubExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.subExpressionArguments) + startSubExpression
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    else -> parseExpressionCommonStart(token)
  }
}

val parseSubExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.subExpressionArguments) + startSubExpression
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    else -> parseExpressionCommonArgument(token)
  }
}
