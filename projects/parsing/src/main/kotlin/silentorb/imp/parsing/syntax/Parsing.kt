package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.*

fun parseSyntax(token: Token, mode: ParsingMode): ParsingTransition {
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
  val (_, state) = tokens
      .fold(ParsingMode.header to newState()) { (mode, state), token ->
        val (nextMode, transition) = parseSyntax(token, mode)
        ((nextMode ?: mode) to transition(token, state))
      }

  val realm = Realm(
      burgs = state.burgs
          .plus(state.burgStack.first().first())
          .map(finalizeBurg(file))
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
