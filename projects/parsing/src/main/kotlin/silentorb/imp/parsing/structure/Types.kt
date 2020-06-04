package silentorb.imp.parsing.structure

import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.ParsingErrors
import silentorb.imp.parsing.general.Token


data class Burg(
    val type: BurgType,
    val range: Range,
    val file: TokenFile
)

typealias BurgId = Int

typealias Roads = Map<BurgId, BurgId>

data class Realm(
    val burgs: Map<BurgId, Burg>,
    val roads: Roads
)

typealias ParsingTransition = (TokenFile, Token, ParsingState) -> ParsingState
