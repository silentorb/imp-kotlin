package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.Token

data class Burg(
    val type: BurgType,
    val range: Range,
    val file: TokenFile,
    val value: Any? = null
) {
  val fileRange: FileRange get() = FileRange(file, range)
}

typealias BurgId = Int

typealias Roads = Map<BurgId, List<BurgId>>

data class Realm(
    val root: BurgId,
    val burgs: Map<BurgId, Burg>,
    val roads: Roads
)

data class PendingParsingError(
    val message: Any,
    val range: Range
)

data class ParsingStep(
    val transition: ParsingStateTransition,
    val mode: ParsingMode? = null,
    val type: BurgType? = null,
    val wrapper: BurgType? = null
)

typealias ParsingStateTransition = (Burg, ParsingState) -> ParsingState

typealias TokenToParsingTransition = (Token) -> ParsingStep

typealias Stack<T> = List<List<T>>

typealias BurgStack = Stack<Burg>

typealias TokenPattern = (Token) -> Boolean
