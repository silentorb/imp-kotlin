package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

typealias ExpressionElementStep = (BurgType, ValueTranslator) -> ParsingStep

fun parseExpressionElement(step: ExpressionElementStep): NullableTokenToParsingTransition = { token ->
  when {
    isNewline(token) -> skip
    isIdentifier(token) || isOperator(token) -> step(BurgType.reference, asString)
    isFloat(token) -> step(BurgType.literalFloat, asFloat)
    isInteger(token) -> step(BurgType.literalInteger, asInt)
    isString(token) -> step(BurgType.literalString, asString)
    else -> null
  }
}

fun parseExpressionCommonStart(transition: ParsingStep) =
    parseExpressionElement { burgType, translator ->
      startSimpleApplication(burgType, translator) + transition
    }

fun parseExpressionCommonArgument(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      startArgument + push(burgType, translator) + goto(mode)
    }

fun parseExpressionFollowingArgument(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      closeArgumentValue + startArgument + push(burgType, translator) + goto(mode)
    }

fun parseExpressionCommonNamedArgumentValue(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      push(burgType, translator) + closeArgumentValue + goto(mode)
    }

val parseExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonStart(goto(ParsingMode.expressionArgumentStart))(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup
        isParenthesesClose(token) -> addError(TextId.missingOpeningParenthesis)
        isDot(token) -> addError(TextId.missingLefthandExpression)
        else -> addError(TextId.missingExpression)
      }
}

val parseDefinitionBodyStart: TokenToParsingTransition = { token ->
  onMatch(isBraceOpen(token)) { startBlock }
      ?: parseExpressionStart(token)
}

val parseExpressionArgumentsCommon: NullableContextualTokenToParsingTransition = { token, contextMode ->
  when {
    isParenthesesOpen(token) -> startGroupArgumentValue
    isParenthesesClose(token) -> tryCloseGroup(contextMode)
    isBraceClose(token) -> tryCloseBlock(contextMode)
    isDot(token) -> startPipingRoot
    else -> null
  }
}

val parseExpressionArgumentStart: ContextualTokenToParsingTransition = { token, contextMode ->
  onMatch(isLet(token)) {
    if (contextMode == ContextMode.group)
      addError(TextId.missingClosingParenthesis) + nextDefinition
    else
      nextDefinition
  }
      ?: parseExpressionCommonArgument(ParsingMode.expressionArgumentFollowing)(token)
      ?: parseExpressionArgumentsCommon(token, contextMode)
      ?: when {
        isEndOfFile(token) -> checkGroupClosed(contextMode) + fold + goto(ParsingMode.block)
        else -> addError(TextId.invalidToken)
      }
}

val parseExpressionFollowingArgument: ContextualTokenToParsingTransition = { token, contextMode ->
  onMatch(isLet(token)) { nextDefinition }
      ?: onMatch(isAssignment(token)) { closeArgumentName }
      ?: parseExpressionFollowingArgument(ParsingMode.expressionArgumentFollowing)(token)
      ?: parseExpressionArgumentsCommon(token, contextMode)
      ?: when {
        isEndOfFile(token) -> checkGroupClosed(contextMode) + closeArgumentValue + fold + goto(ParsingMode.block)
        else -> addError(TextId.invalidToken)
      }
}

val parseExpressionNamedArgumentValue: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonNamedArgumentValue(ParsingMode.expressionArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup
        isNewline(token) -> skip
        isEndOfFile(token) -> addError(TextId.missingExpression)
        else -> addError(TextId.invalidToken)
      }
}
