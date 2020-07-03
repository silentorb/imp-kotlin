package silentorb.imp.parsing.syntax

import silentorb.imp.core.TokenFile

data class ParsingState(
    val accumulator: Set<Burg>,
    val burgStack: BurgStack,
    val contextStack: List<ContextMode>,
    val errors: List<PendingParsingError>,
    val mode: ParsingMode
)

fun newState(file: TokenFile, mode: ParsingMode) =
    ParsingState(
        accumulator = setOf(),
        burgStack = listOf(listOf(newRootBurg(file))),
        contextStack = listOf(),
        errors = listOf(),
        mode = mode
    )
