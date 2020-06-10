package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

typealias ExpressionElementStep = (BurgType, ValueTranslator) -> ParsingStep

fun parseExpressionElement(step: ExpressionElementStep): NullableTokenToParsingTransition = { token ->
  when {
    isNewline(token) -> skipStep
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
          startArgument
              + push(burgType, translator)
          , mode)
    }

fun parseExpressionFollowingArgument(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      ParsingStep(
          closeArgumentValue
              + startArgument
              + push(burgType, translator)
          , mode)
    }

fun parseExpressionCommonNamedArgumentValue(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      ParsingStep(
          push(burgType, translator)
              + closeArgumentValue
//              + pop
//              + pop
//              + pop
          , mode)
    }

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.expressionRootArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentStart) + startGroup
        isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
        isDot(token) -> parsingError(TextId.missingLefthandExpression)
        else -> parsingError(TextId.missingExpression)
      }
}

val parseRootExpressionArgumentsCommon: NullableTokenToParsingTransition = { token ->
  when {
    isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentStart) + startGroupArgumentValue
    isParenthesesClose(token) -> parsingError(TextId.missingOpeningParenthesis)
    isDot(token) -> startPipingRoot
    else -> null
  }
}

val parseRootExpressionArgumentStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: parseExpressionCommonArgument(ParsingMode.expressionRootArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isEndOfFile(token) -> ParsingStep(fold, ParsingMode.body)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseRootExpressionFollowingArgument: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { nextDefinition }
      ?: parseExpressionFollowingArgument(ParsingMode.expressionRootArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token)
      ?: when {
        isAssignment(token) -> closeArgumentName
        isEndOfFile(token) -> ParsingStep(closeArgumentValue + fold, ParsingMode.body)
        else -> parsingError(TextId.invalidToken)
      }
}

val parseExpressionRootNamedArgumentValue: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonNamedArgumentValue(ParsingMode.expressionRootArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> onReturn(ParsingMode.expressionRootArgumentStart) + startGroupNamedArgumentValue
        isNewline(token) -> skipStep
        isEndOfFile(token) -> parsingError(TextId.missingExpression)
        else -> parsingError(TextId.invalidToken)
      }
}
