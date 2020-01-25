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
