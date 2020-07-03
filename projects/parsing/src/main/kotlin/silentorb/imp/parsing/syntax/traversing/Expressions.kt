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
    else -> null
  }
}

fun parseExpressionCommonStart(mode: ParsingMode) =
    parseExpressionElement { burgType, translator ->
      startSimpleApplication(burgType, translator) + goto(mode)
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

val parseRootExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonStart(ParsingMode.expressionArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) ->startGroup
        isParenthesesClose(token) -> addError(TextId.missingOpeningParenthesis)
        isDot(token) -> addError(TextId.missingLefthandExpression)
        else -> addError(TextId.missingExpression)
      }
}

val parseDefinitionExpressionStart: TokenToParsingTransition = { token ->
  onMatch(isBraceOpen(token)) { startBlock }
      ?: parseRootExpressionStart(token)
}

val parseRootExpressionArgumentsCommon: NullableContextualTokenToParsingTransition = { token, contextMode ->
  when {
    isParenthesesOpen(token) -> startGroupArgumentValue
    isParenthesesClose(token) && contextMode == ContextMode.group -> closeGroup
    isParenthesesClose(token) -> addError(TextId.missingOpeningParenthesis)
    isDot(token) -> startPipingRoot
    else -> null
  }
}

val parseRootExpressionArgumentStart: ContextualTokenToParsingTransition = { token, contextMode ->
  onMatch(isLet(token)) {
    if (contextMode == ContextMode.group)
      addError(TextId.missingClosingParenthesis) + nextDefinition
    else
      nextDefinition
  }
      ?: parseExpressionCommonArgument(ParsingMode.expressionArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token, contextMode)
      ?: when {
        isEndOfFile(token) -> checkGroupClosed(contextMode) + fold + goto(ParsingMode.body)
        else -> addError(TextId.invalidToken)
      }
}

val parseRootExpressionFollowingArgument: ContextualTokenToParsingTransition = { token, contextMode ->
  onMatch(isLet(token)) { nextDefinition }
      ?: onMatch(isAssignment(token)) { closeArgumentName }
      ?: parseExpressionFollowingArgument(ParsingMode.expressionArgumentFollowing)(token)
      ?: parseRootExpressionArgumentsCommon(token, contextMode)
      ?: when {
        isEndOfFile(token) -> checkGroupClosed(contextMode) + closeArgumentValue + fold + goto(ParsingMode.body)
        else -> addError(TextId.invalidToken)
      }
}

val parseExpressionRootNamedArgumentValue: TokenToParsingTransition = { token ->
  onMatch(isLet(token)) { addError(TextId.missingExpression) + nextDefinition }
      ?: parseExpressionCommonNamedArgumentValue(ParsingMode.expressionArgumentStart)(token)
      ?: when {
        isParenthesesOpen(token) -> startGroup
        isNewline(token) -> skip
        isEndOfFile(token) -> addError(TextId.missingExpression)
        else -> addError(TextId.invalidToken)
      }
}
