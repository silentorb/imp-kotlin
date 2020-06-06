package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.Token

data class Burg(
    val type: BurgType,
    val range: Range,
    val file: TokenFile,
    val children: List<BurgId>,
    val value: Any?
) {
  val fileRange: FileRange get() = FileRange(file, range)

  init {
    assert(value == null || children.none())
  }
}

typealias BurgId = Int

typealias Roads = Map<BurgId, List<BurgId>>

data class Realm(
    val root: BurgId,
    val burgs: Map<BurgId, Burg>
) {
  val roads: Roads get() = burgs.mapValues { it.value.children }
}

data class PendingParsingError(
    val message: Any,
    val range: Range
)

data class ParsingStep(
    val transition: ParsingStateTransition,
    val mode: ParsingMode? = null,
    val consume: Boolean = true
)

typealias ValueTranslator = (String) -> Any?

typealias NewBurg = (BurgType, ValueTranslator) -> Burg
typealias ParsingStateTransition = (NewBurg, ParsingState) -> ParsingState

typealias TokenToParsingTransition = (Token) -> ParsingStep

typealias Stack<T> = List<List<T>>

typealias BurgStack = Stack<Burg>

typealias TokenPattern = (Token) -> Boolean
