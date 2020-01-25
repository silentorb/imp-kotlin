package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.*

typealias CharPattern = (Char) -> Boolean

typealias Consumer<T> = (CodeBuffer, Position) -> T

fun patternFromRegex(regex: Regex): CharPattern =
    { it.toString().matches(regex) }

fun patternFromRegex(string: String): CharPattern =
    patternFromRegex(Regex(string))

fun consumeSingle(pattern: CharPattern, code: CodeBuffer, end: Position): Char? {
  val character = nextCharacter(code, end.index)
  return if (character != null && pattern(character))
    character
  else
    null
}

data class Bundle(
    val code: CodeBuffer,
    val start: Position,
    val end: Position = start,
    val buffer: LexicalBuffer = newLexicalBuffer()
)

fun consumeSingle(bundle: Bundle, pattern: CharPattern): Char? =
    consumeSingle(pattern, bundle.code, bundle.end)

fun incrementBundle(character: Char, bundle: Bundle): Bundle =
    bundle.copy(
        end = nextPosition(character, bundle.end),
        buffer = appendToLexicalBuffer(bundle.buffer, character)
    )
