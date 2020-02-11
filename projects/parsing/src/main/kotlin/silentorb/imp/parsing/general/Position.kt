package silentorb.imp.parsing.general

data class Position(
    val index: CodeInt,
    val column: CodeInt,
    val row: CodeInt
)

fun newPosition() =
    Position(
        index = 0,
        column = 1,
        row = 1
    )

fun formatPositionString(position: Position): String =
    "${position.row}:${position.column}"

data class Range(
    val start: Position,
    val end: Position = start
)
