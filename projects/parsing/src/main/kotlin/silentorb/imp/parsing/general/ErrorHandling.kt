package silentorb.imp.parsing.general

fun newParsingError(message: TextId, token: Token, arguments: List<Any> = listOf()) =
    ParsingError(
        message = message,
        range = token.range,
        arguments = arguments
    )

fun newParsingError(message: TextId): (Token) -> ParsingError = { token ->
  ParsingError(
      message = message,
      range = token.range
  )
}

fun errorIf(condition: Boolean, message: TextId, token: Token): ParsingError? =
    if (condition)
      newParsingError(message, token)
    else
      null
