package silentorb.imp.parsing.syntax

data class ParsingState(
    val burgs: Set<PendingBurg>,
    val burgStack: BurgStack,
    val errors: List<PendingParsingError>,
    val modeStack: List<ParsingMode>
)

fun newState() =
    ParsingState(
        burgs = setOf(),
        burgStack = listOf(listOf(rootBurg())),
        errors = listOf(),
        modeStack = listOf()
    )
