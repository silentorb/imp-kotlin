package silentorb.imp.parsing

import silentorb.imp.parsing.lexing.Range
import silentorb.imp.parsing.lexing.Token

data class ParsingError(
    val message: String,
    val token: Token?,
    val range: Range
)
