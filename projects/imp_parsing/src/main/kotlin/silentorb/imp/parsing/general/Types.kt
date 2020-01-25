package silentorb.imp.parsing.general

import silentorb.imp.parsing.lexer.Rune

data class Range(
    val start: Position,
    val end: Position = start
)


data class Token(
    val rune: Rune,
    val range: Range,
    val text: String? = null
)

typealias Tokens = List<Token>
