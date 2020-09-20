package silentorb.imp.parsing.syntaxNew

import silentorb.imp.core.ImpError
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.syntax.*

enum class ImportPathMode {
  dot,
  identifier
}

tailrec fun importPath(
    mode: ImportPathMode,
    tokens: Tokens,
    burgs: List<NestedBurg> = listOf()
): ParsingResponse {
  val token = tokens.first()
  val nextMode = if (mode == ImportPathMode.identifier)
    ImportPathMode.dot
  else
    ImportPathMode.identifier

  val (burg, errors) = when (mode) {
    ImportPathMode.identifier -> when {
      isIdentifier(token) -> newNestedBurg(BurgType.importPathToken, token, value = token.value) to listOf()
      isWildcard(token) -> newNestedBurg(BurgType.importPathWildcard, token) to listOf()
      else -> null to listOf(ImpError(TextId.expectedIdentifierOrWildcard, token.fileRange))
    }
    ImportPathMode.dot -> when {
      isDot(token) -> null to listOf()
      isNewline(token) -> null to listOf()
      else -> null to listOf(ImpError(TextId.unexpectedCharacter, token.fileRange))
    }
  }

  return if (errors.any() || isNewline(token) || isWildcard(token))
    ParsingResponse(tokens, burgs, errors)
  else
    importPath(nextMode, tokens.drop(1), burgs + listOfNotNull(burg))
}

val importFirstPathToken: ParsingFunction =
    consumeExpected(::isIdentifier, TextId.missingImportPath, consumeToken(BurgType.importPathToken)) +
        { tokens -> importPath(ImportPathMode.dot, tokens) }

val importClause: ParsingFunction = wrap(BurgType.importClause, consume, importFirstPathToken)

val expressionCommon: OptionalRouter = { token ->
  when {
//    isNewline(token) -> consume
    isIdentifier(token) || isOperator(token) -> consumeToken(BurgType.reference)
    isInteger(token) -> consumeToken(BurgType.literalInteger, asInt)
    isFloat(token) -> consumeToken(BurgType.literalFloat, asFloat)
    isString(token) -> consumeToken(BurgType.literalString, asString)
    else -> null
  }
}

val expressionFollowing: ParsingFunction =
    parsingLoop(
        route { token ->
          expressionCommon(token) ?: when {
            isNewline(token) -> consume
            isLet(token) -> exitLoop
            isEndOfFile(token) -> exitLoop
            else -> addError(TextId.missingExpression)
          }
        }
    )

val expressionStart: ParsingFunction = route { token ->
  expressionCommon(token) ?: when {
    else -> addError(TextId.missingExpression)
  }
}

val expressionStartWrapper: ParsingFunction = { tokens ->
  val start = expressionStart(tokens)
  if (start.errors.any())
    start
  else {
    val nextFunction = expressionCommon(start.tokens.first())
    if (nextFunction == null)
      start
    else {
      val next = nextFunction(start.tokens)
      val (furtherTokens, furtherBurgs, furtherErrors) = expressionFollowing(next.tokens)
      val arguments = (next.burgs + furtherBurgs)
          .map { burg ->
            newNestedBurg(BurgType.argument, listOf(newNestedBurg(BurgType.argumentValue, listOf(burg))))
          }
      val application = newNestedBurg(
          type = BurgType.application,
          children = listOf(newNestedBurg(BurgType.appliedFunction, start.burgs)) + arguments
      )
      ParsingResponse(furtherTokens, burgs = listOf(application), errors = furtherErrors)
    }
  }
}

val expression: ParsingFunction = expressionStartWrapper

val definition: ParsingFunction = wrap(
    BurgType.definition,
    consume, // let token
    consumeExpected(::isIdentifier, TextId.expectedIdentifier, consumeToken(BurgType.definitionName)),
    consumeExpected(::isAssignment, TextId.expectedAssignment),
    expression
)

val header: ParsingFunction =
    parsingLoop(
        route { token ->
          when {
            isImport(token) -> importClause
            isLet(token) -> definition
            isNewline(token) -> consume
            isEndOfFile(token) -> exitLoop
            else -> addError(TextId.expectedImportOrLetKeywords)
          }
        }
    )

val parseTokens: ParsingFunction = wrap(BurgType.block, header)
