package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.failure
import silentorb.imp.parsing.general.success
import silentorb.imp.parsing.general.*

data class TokenResult(
    val token: Token?,
    val position: Position
)

fun nextCharacter(code: CodeBuffer, index: CodeInt): Char? {
  val size = getCodeBufferSize(code)
  return if (index > size - 1)
    null
  else
    getCharFromBuffer(code, index)
}

fun getNextMode(character: Char?, mode: LexicalMode, position: Position): Response<LexicalMode?> {
  return if (character == null) {
    success(null)
  } else {
    val nextMode = processChar(character, mode)
    if (nextMode == null)
      failure(listOf(LexicalError(TextId.unexpectedCharacter, range = Range(position))))
    else
      success(nextMode)
  }
}

tailrec fun nextToken(code: CodeBuffer, start: Position, end: Position, buffer: LexicalBuffer, mode: LexicalMode): Response<TokenResult> {
  val character = nextCharacter(code, end.index)
  val modeResult = getNextMode(character, mode, end)
  return when (modeResult) {
    is Response.Failure -> failure(modeResult.errors)
    is Response.Success -> {
      val (nextMode) = modeResult
      if (nextMode == null) {
        success(TokenResult(
            token = newToken(mode, Range(start, end), buffer),
            position = nextPosition(character, end)
        ))
      } else
        nextToken(code, start, nextPosition(character, end), buffer, nextMode)
    }
  }
}

fun singleCharacterTokenMatch(code: CodeBuffer, position: Position): Response<TokenResult>? {
  val character = nextCharacter(code, position.index)
  return if (character != null) {
    val rune = singleCharacterTokens(character)
    if (rune != null)
      success(TokenResult(
          token = Token(rune, Range(position, position)),
          position = nextPosition(character, position)
      ))
    else
      null
  } else
    null
}

fun consumeWhitespace(code: CodeBuffer, position: Position):

fun nextToken(code: CodeBuffer, position: Position): Response<TokenResult> =
    singleCharacterTokenMatch(code, position) ?: nextToken(code, position, position, "", LexicalMode.fresh)

tailrec fun tokenize(code: CodeBuffer, position: Position, tokens: Tokens): Response<Tokens> {
  val result = nextToken(code, position)
  return when (result) {
    is Response.Failure -> failure(result.errors)
    is Response.Success -> {
      val (token, newPosition) = result.value
      if (token == null)
        success(tokens)
      else
        tokenize(code, newPosition, tokens.plus(token))
    }
  }
}

fun tokenize(code: CodeBuffer): Response<Tokens> =
    tokenize(code, position = newPosition(), tokens = listOf())
