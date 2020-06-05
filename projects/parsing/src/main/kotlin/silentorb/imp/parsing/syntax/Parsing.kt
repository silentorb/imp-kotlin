package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.core.newPosition
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.traversing.*

fun parseDescent(mode: ParsingMode?): TokenToParsingTransition = { _ ->
  // Switching to ParsingMode.body as the next best thing to throwing an error
  ParsingStep(skip, mode ?: ParsingMode.body)
}

fun getTransition(token: Token, mode: ParsingMode, lowerMode: ParsingMode?): ParsingStep {
  val action: TokenToParsingTransition =
      when (mode) {
        ParsingMode.descend -> parseDescent(lowerMode)
        ParsingMode.header -> parseHeader
        ParsingMode.importFirstPathToken -> parseImportFirstPathToken
        ParsingMode.importFollowingPathToken -> parseImportFollowingPathToken
        ParsingMode.importSeparator -> parseImportSeparator
        ParsingMode.body -> parseBody
        ParsingMode.definitionAssignment -> parseDefinitionAssignment
        ParsingMode.definitionParameterColon -> parseDefinitionParameterColon
        ParsingMode.definitionParameterSeparatorOrAssignment -> parseDefinitionParameterSeparatorOrAssignment
        ParsingMode.definitionParameterName -> parseDefinitionParameterName
        ParsingMode.definitionParameterNameOrAssignment -> parseDefinitionParameterNameOrAssignment
        ParsingMode.definitionParameterType -> parseDefinitionParameterType
        ParsingMode.definitionName -> parseDefinitionName
        ParsingMode.expression -> parseDefinitionExpression
        ParsingMode.subExpression -> parseSubExpression
      }

  return action(token)
}

fun newBurg(file: TokenFile, token: Token): NewBurg = { burgType, valueTranslator ->
  Burg(
      type = burgType,
      range = token.range,
      file = file,
      children = listOf(),
      value = valueTranslator(token.value)
  )
}

tailrec fun parsingStep(
    file: TokenFile,
    tokens: Tokens,
    mode: ParsingMode,
    state: ParsingState
): ParsingState =
    if (tokens.none())
      state
    else {
      val token = tokens.first()
      val lowerMode = state.modeStack.lastOrNull()
      val (transition, requestedMode) = getTransition(token, mode, lowerMode)
      val nextMode = requestedMode ?: mode
      val nextState = transition(newBurg(file, token), state)
      val nextTokens = if (mode == ParsingMode.descend)
        tokens
      else
        tokens.drop(1)

      parsingStep(file, nextTokens, nextMode, nextState)
    }

fun parseSyntax(file: TokenFile, tokens: Tokens): ParsingResponse<Realm> {
  val sanitizedTokens = if (tokens.size == 0 || tokens.last().rune != Rune.newline)
    tokens + Token(Rune.newline, FileRange("", Range(newPosition(), newPosition())), "")
  else
    tokens

  val state = parsingStep(file, sanitizedTokens, ParsingMode.header, newState(file))

  assert(state.burgStack.size == 1)

  val root = state.burgStack.first().first()
  val realm = Realm(
      root = root.hashCode(),
      burgs = state.accumulator
          .plus(root)
          .associateBy { it.hashCode() }
  )

  return ParsingResponse(
      realm,
      state.errors.map { error ->
        ParsingError(
            message = error.message,
            fileRange = FileRange(file, error.range)
        )
      }
  )
}
