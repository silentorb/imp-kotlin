package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.*

typealias CharPattern = (Char) -> Boolean

typealias Consumer<T> = (CodeBuffer, Position) -> T

fun patternFromRegex(regex: Regex): CharPattern =
    { it.toString().matches(regex) }

fun patternFromRegex(string: String): CharPattern =
    patternFromRegex(Regex(string))

fun patternFromChar(value: Char): CharPattern =
    { it == value }

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
    val end: Position,
    val buffer: LexicalBuffer = newLexicalBuffer()
)

fun consumeSingle(bundle: Bundle, pattern: CharPattern): Char? =
    consumeSingle(pattern, bundle.code, bundle.end)

fun incrementBundle(character: Char, bundle: Bundle): Bundle =
    bundle.copy(
        end = nextPosition(character, bundle.end),
        buffer = appendToLexicalBuffer(bundle.buffer, character)
    )

data class TokenStep(
    val position: Position,
    val token: Token? = null
)

fun tokenFromBundle(rune: Rune): (Bundle) -> Response<TokenStep> = { bundle ->
  success(TokenStep(
      token = Token(
          rune = rune,
          range = Range(bundle.start, bundle.end),
          value = bundle.buffer
      ),
      position = bundle.end
  ))
}
