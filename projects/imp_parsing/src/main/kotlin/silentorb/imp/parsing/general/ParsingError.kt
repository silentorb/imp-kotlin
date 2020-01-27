package silentorb.imp.parsing.general

data class ParsingError(
    val message: TextId,
    val range: Range,
    val token: Token? = null
)

fun newParsingError(message: TextId, token: Token) =
    ParsingError(
        message = message,
        range = token.range,
        token = token
    )

fun newParsingError(message: TextId): (Token) -> ParsingError = { token ->
  ParsingError(
      message = message,
      range = token.range,
      token = token
  )
}

fun newParsingError(message: TextId, range: Range) =
    ParsingError(
        message = message,
        range = range
    )

fun errorIf(condition: Boolean, message: TextId, token: Token): ParsingError? =
    if (condition)
      newParsingError(message, token)
    else
      null

fun errorIf(condition: Boolean, message: TextId, range: Range): ParsingError? =
    if (condition)
      ParsingError(message, range = range)
    else
      null
