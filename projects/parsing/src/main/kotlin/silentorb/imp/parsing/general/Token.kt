package silentorb.imp.parsing.general

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.Burg
import java.nio.file.Path

data class Token(
    val rune: Rune,
    val fileRange: FileRange,
    val value: String
) {
  val range: Range get() = fileRange.range
  val file: TokenFile get() = fileRange.file
}

typealias Tokens = List<Token>

// Assumes the tokens are in the same order as they appear in the source code
fun tokensToRange(tokens: Tokens): Range =
    Range(tokens.first().range.start, tokens.last().range.end)

fun tokensToFileRange(tokens: Tokens): FileRange =
    FileRange(tokens.first().file, tokensToRange(tokens))

fun burgsToRange(burgs: List<Burg>): Range =
    Range(burgs.first().range.start, burgs.last().range.end)

fun burgsToFileRange(burgs: List<Burg>): FileRange =
    FileRange(burgs.first().file, burgsToRange(burgs))
