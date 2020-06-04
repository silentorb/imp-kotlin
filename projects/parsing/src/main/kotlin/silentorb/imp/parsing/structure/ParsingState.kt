package silentorb.imp.parsing.structure

import silentorb.imp.parsing.general.ParsingErrors

data class ParsingState(
    val burgs: Map<BurgId, Burg>,
    val errors: ParsingErrors,
    val mode: ParsingMode,
    val burgStack: List<BurgId>,
    val roads: Roads
)

fun newState() =
    ParsingState(
        burgs = mapOf(),
        errors = listOf(),
        mode = ParsingMode.header,
        burgStack = listOf(),
        roads = mapOf()
    )
