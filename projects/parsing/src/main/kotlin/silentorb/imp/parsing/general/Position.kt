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

data class Range(
    val start: Position,
    val end: Position = start
)

fun positionString(position: Position): String =
    "${position.row}:${position.column}"

fun rangeString(range: Range): String =
    if (range.start == range.end)
      positionString(range.start)
    else
      "${positionString(range.start)} - ${positionString(range.end)}"

fun isInRange(range: Range, offset: Int): Boolean =
    offset >= range.start.index && offset <= range.end.index
