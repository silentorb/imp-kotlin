package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

typealias ExpressionElementStep = (BurgType, ValueTranslator) -> ParsingStep

fun parseExpressionElement(step: ExpressionElementStep): TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> step(BurgType.reference, asString)
    isFloat(token) -> step(BurgType.literalFloat, asFloat)
    isInteger(token) -> step(BurgType.literalInteger, asInt)
    isNewline(token) -> skipStep
    else -> parsingError(TextId.invalidToken)
  }
}

fun parseExpressionCommonStart(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      ParsingStep(
          pushMarker(BurgType.application)
              + pushMarker(BurgType.appliedFunction)
              + push(burgType, translator)
              + pop
              + pop
              + pushEmpty
          , mode)
    }

fun parseExpressionCommonArgument(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      ParsingStep(
          pushMarker(BurgType.argument)
              + pushMarker(BurgType.argumentValue)
              + push(burgType, translator)
              + pop
              + pop
              + popAppend
          , mode)
    }

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    else -> parseExpressionCommonStart(ParsingMode.expressionRootArguments)(token)
  }
}
val startExpression = push(BurgType.expression, asMarker) + parseRootExpressionStart

val parseRootExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    isEndOfFile(token) -> ParsingStep(fold)
    else -> parseExpressionCommonArgument(ParsingMode.expressionRootArguments)(token)
  }
}

val parseSubExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startGroup
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    else -> parseExpressionCommonStart(ParsingMode.groupArguments)(token)
  }
}

val parseSubExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.groupArguments) + startGroup
    isParenthesesClose(token) -> descend
    isLet(token) -> addError(TextId.missingClosingParenthesis) + nextDefinition
    else -> parseExpressionCommonArgument(ParsingMode.groupArguments)(token)
  }
}
