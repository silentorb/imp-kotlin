package silentorb.imp.parsing.syntax

import silentorb.imp.core.FileRange
import silentorb.imp.core.Position
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token

data class PendingBurg(
    val type: BurgType,
    val range: Range,
    val value: Any? = null,
    val children: List<BurgId> = listOf()
)

data class Burg(
    val type: BurgType,
    val range: Range,
    val file: TokenFile,
    val value: Any? = null,
    val children: List<BurgId>
)

typealias BurgId = Int

typealias Roads = Map<BurgId, List<BurgId>>

data class Realm(
    val burgs: Map<BurgId, Burg>
)

data class PendingParsingError(
    val message: Any,
    val range: Range
)

typealias ParsingStateTransition = (Token, ParsingState) -> ParsingState

typealias ParsingTransition = Pair<ParsingMode?, ParsingStateTransition>
typealias TokenToParsingTransition = (Token) -> ParsingTransition

typealias Stack<T> = List<List<T>>

typealias BurgStack = Stack<PendingBurg>
