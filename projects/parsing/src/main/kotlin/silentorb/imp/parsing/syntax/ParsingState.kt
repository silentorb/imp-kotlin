package silentorb.imp.parsing.syntax

data class ParsingState(
    // Used for gathering data.  Should be written to by the parsing router but not effect the parsing router
    val burgStack: BurgStack,

    // Used for tracking advanced parsing state.  Can effect the parsing router.
    val contextStack: List<ContextMode>,

    val errors: List<PendingParsingError>,
    val mode: ParsingMode
)

fun newState(mode: ParsingMode) =
    ParsingState(
        burgStack = listOf(BurgLayer(type = BurgType.block)),
        contextStack = listOf(),
        errors = listOf(),
        mode = mode
    )
