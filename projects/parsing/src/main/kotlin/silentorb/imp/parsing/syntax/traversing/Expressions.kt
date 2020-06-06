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
          startApplication(burgType, translator)
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
              + pop
          , mode)
    }

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    isDot(token) -> parsingError(TextId.missingLefthandExpression)
    else -> parseExpressionCommonStart(ParsingMode.expressionRootArguments)(token)
  }
}

val parseRootExpressionArguments: TokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArguments) + startArgument + startGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isLet(token) -> nextDefinition
    isEndOfFile(token) -> ParsingStep(fold)
    isDot(token) -> startPipingRoot
    else -> parseExpressionCommonArgument(ParsingMode.expressionRootArguments)(token)
  }
}
