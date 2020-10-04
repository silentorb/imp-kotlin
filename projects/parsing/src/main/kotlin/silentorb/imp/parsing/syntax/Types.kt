package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.Token

data class Burg(
    val type: BurgType,
    val range: Range,
    val children: List<Burg>,
    val value: Any? = null
) {
  val file: TokenFile get() = range.start.file
  val fileRange: FileRange get() = FileRange(file, range)

  init {
    if (file != range.start.file) {
      val k = 0
    }
  }
}

typealias BurgId = Int

data class Realm(
    val root: Burg,
    val burgs: Set<Burg>
)

data class PendingParsingError(
    val message: Any,
    val range: Range
)

typealias ValueTranslator = (String) -> Any?

typealias NewBurg = (BurgType, ValueTranslator) -> Burg
typealias ParsingStateTransition = (NewBurg, ParsingState) -> ParsingState
typealias ParsingStep = ParsingStateTransition

typealias TokenToParsingTransition = (Token) -> ParsingStep
typealias NullableTokenToParsingTransition = (Token) -> ParsingStep?

data class BurgLayer(
    val type: Any? = null,
    val burgs: List<Burg> = listOf()
)

typealias BurgStack = List<BurgLayer>
