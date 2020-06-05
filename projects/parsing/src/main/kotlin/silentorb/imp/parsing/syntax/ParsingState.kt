package silentorb.imp.parsing.syntax

import silentorb.imp.core.TokenFile

data class ParsingState(
    val accumulator: Set<Burg>,
    val burgStack: BurgStack,
    val errors: List<PendingParsingError>,
    val modeStack: List<ParsingMode>
)

fun newState(file: TokenFile) =
    ParsingState(
        accumulator = setOf(),
        burgStack = listOf(listOf(newRootBurg(file))),
        errors = listOf(),
        modeStack = listOf()
    )
