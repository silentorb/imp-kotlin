package silentorb.imp.parsing.lexing

data class Position(
    val index: Long,
    val column: Long,
    val row: Long
)

data class Range(
    val start: Position,
    val end: Position
)

enum class Rune {
  assignment,
  identifier,
  literalFloat,
}

data class Token(
    val rune: Rune,
    val text: String,
    val range: Range
)
