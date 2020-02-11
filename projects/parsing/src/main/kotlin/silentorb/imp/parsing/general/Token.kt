package silentorb.imp.parsing.general

import silentorb.imp.parsing.lexer.Rune

data class Token(
    val rune: Rune,
    val range: Range,
    val value: String
)

typealias Tokens = List<Token>

// Assumes the tokens are in the same order as they appear in the source code
fun tokensToRange(tokens: Tokens): Range =
    Range(tokens.first().range.start, tokens.last().range.end)
