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

  return if (errors.any() || isNewline(token))
    ParsingResponse(tokens, burgs, errors)
  else
    importPath(nextMode, tokens.drop(1), burgs + listOfNotNull(burg))
}

val importFirstPathToken: ParsingFunction =
    consumeExpected(::isIdentifier, TextId.expectedIdentifier, consumeToken(BurgType.importPathToken)) +
    { tokens -> importPath(ImportPathMode.dot, tokens) }

val importClause: ParsingFunction = wrap(BurgType.importClause, consume, importFirstPathToken)

val expression: ParsingFunction = route { token ->
  when {
    isInteger(token) -> consumeToken(BurgType.literalInteger, asInt)
    isFloat(token) -> consumeToken(BurgType.literalFloat, asFloat)
    isString(token) -> consumeToken(BurgType.literalString, asString)
    else -> addError(TextId.expectedAssignment)
  }
}

val definition: ParsingFunction = wrap(
    BurgType.definition,
    consume, // let token
    consumeExpected(::isIdentifier, TextId.expectedIdentifier, consumeToken(BurgType.definitionName)),
    consumeExpected(::isAssignment, TextId.expectedAssignment),
    expression
)

val header: ParsingFunction = route { token ->
  when {
    isImport(token) -> importClause
    isLet(token) -> definition
    isNewline(token) -> addError(TextId.expectedImportOrLetKeywords)
    isEndOfFile(token) -> throw Error("Not implemented") //goto(ParsingMode.block)
    else -> addError(TextId.expectedImportOrLetKeywords)
  }
}

val parseTokens: ParsingFunction = wrap(BurgType.block, header)
