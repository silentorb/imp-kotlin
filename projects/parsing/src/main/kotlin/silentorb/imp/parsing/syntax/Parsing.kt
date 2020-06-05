package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.core.newPosition
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.traversing.*

fun parseSyntax(token: Token, mode: ParsingMode): ParsingStep {
  val action: TokenToParsingTransition =
      when (mode) {
        ParsingMode.header -> ::parseHeader
        ParsingMode.importFirstPathToken -> ::parseImportFirstPathToken
        ParsingMode.importFollowingPathToken -> ::parseImportFollowingPathToken
        ParsingMode.importSeparator -> ::parseImportSeparator
        ParsingMode.body -> parseBody
        ParsingMode.definitionAssignment -> parseDefinitionAssignment
        ParsingMode.definitionParameterColon -> parseDefinitionParameterColon
        ParsingMode.definitionParameterSeparatorOrAssignment -> parseDefinitionParameterSeparatorOrAssignment
        ParsingMode.definitionParameterName -> parseDefinitionParameterName
        ParsingMode.definitionParameterNameOrAssignment -> parseDefinitionParameterNameOrAssignment
        ParsingMode.definitionParameterType -> parseDefinitionParameterType
        ParsingMode.definitionName -> parseDefinitionName
        ParsingMode.definitionExpression -> parseExpression(ParsingMode.body)
      }

  return action(token)
}

fun parseSyntax(file: TokenFile, tokens: Tokens): ParsingResponse<Realm> {
  val sanitizedTokens = if (tokens.size == 0 || tokens.last().rune != Rune.newline)
    tokens + Token(Rune.newline, FileRange("", Range(newPosition(), newPosition())), "")
  else
    tokens

  val (finalMode, state) = sanitizedTokens
      .fold(ParsingMode.header to newState(file)) { (mode, state), token ->
        val (transition, nextMode) = parseSyntax(token, mode)
        val newBurg: NewBurg = { burgType, valueTranslator ->
          Burg(
              type = burgType,
              range = token.range,
              file = file,
              children = listOf(),
              value = valueTranslator(token.value)
          )
        }
        ((nextMode ?: mode) to transition(newBurg, state))
      }

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
