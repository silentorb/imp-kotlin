package silentorb.imp.core

import java.net.URI

// Could be later changed to Long
typealias CodeInt = Int

data class Position(
    val index: CodeInt,
    val file: TokenFile,
    val column: CodeInt,
    val row: CodeInt
)

fun newPosition(file: TokenFile = "") =
    Position(
        index = 0,
        column = 1,
        row = 1,
        file = file
    )

data class Range(
    val start: Position,
    val end: Position = start
) {
  val length: Int get() = end.index - start.index
}

typealias TokenFile = String

data class FileRange(
    val file: TokenFile,
    val range: Range
) {
  init {
    if (file != range.start.file) {
      val k = 0
    }
  }
}

fun emptyFileRange(): FileRange =
    FileRange("", Range(newPosition(), newPosition()))

fun positionString(position: Position): String =
    "${position.row}:${position.column}"

fun rangeString(range: Range): String =
    if (range.start == range.end)
      positionString(range.start)
    else
      "${positionString(range.start)} - ${positionString(range.end)}"

fun rangeString(fileRange: FileRange): String =
    "${fileRange.file}: ${rangeString(fileRange.range)}"

fun isInRange(range: Range, offset: Int): Boolean =
    offset >= range.start.index && offset <= range.end.index

fun isInRange(fileRange: FileRange, file: String, offset: Int): Boolean =
    fileRange.file == file && offset >= fileRange.range.start.index && offset <= fileRange.range.end.index
