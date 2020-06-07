package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

typealias ExpressionElementStep = (BurgType, ValueTranslator) -> ParsingStep

fun parseExpressionElement(step: ExpressionElementStep): NullableTokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> step(BurgType.reference, asString)
    isFloat(token) -> step(BurgType.literalFloat, asFloat)
    isInteger(token) -> step(BurgType.literalInteger, asInt)
    else -> null
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
          startUnnamedArgument
              + push(burgType, translator)
              + pop
              + pop
              + pop
          , mode)
    }

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.expressionRootArgumentValueBeforeNewline)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentValueBeforeNewline) + startGroup
        isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        isNewline(token) -> skipStep
        else -> parsingError(TextId.missingExpression)
      }
}

val parseRootExpressionArgumentsCommon: NullableTokenToParsingTransition = { token ->
  parseExpressionCommonArgument(ParsingMode.expressionRootArgumentValueOrNamedBeforeNewline)(token) ?: when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentValueBeforeNewline) + startUnnamedArgument + startGroup
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isEndOfFile(token) -> ParsingStep(fold)
    isDot(token) -> startPipingRoot
    else -> null
  }
}

val parseRootExpressionArgumentsAfterNewline: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isNewline(token) -> ParsingStep(skip, ParsingMode.expressionRootArgumentValueAfterNewline)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseRootExpressionArgumentsBeforeNewline: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.expectedNewline) + nextDefinition }
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isNewline(token) -> ParsingStep(skip, ParsingMode.expressionRootArgumentValueAfterNewline)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseRootExpressionFollowingArgumentAfterNewline: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isAssignment(token) -> switchToNamedArgument
        isNewline(token) -> ParsingStep(skip, ParsingMode.expressionRootArgumentValueAfterNewline)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseRootExpressionFollowingArgumentBeforeNewline: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.expectedNewline) + nextDefinition }
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isAssignment(token) -> switchToNamedArgument
        isNewline(token) -> ParsingStep(skip, ParsingMode.expressionRootArgumentValueAfterNewline)
        else -> parsingError(TextId.invalidToken)
      }
}
