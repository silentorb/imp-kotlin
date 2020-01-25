package silentorb.imp.parsing.general

data class LexicalError(
    val message: TextId,
    val token: Token? = null,
    val range: Range
)
