package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.failure
import silentorb.imp.parsing.general.success
import silentorb.imp.parsing.general.*

data class TokenStep(
    val position: Position,
    val token: Token? = null
)

fun nextCharacter(code: CodeBuffer, index: CodeInt): Char? {
  val size = getCodeBufferSize(code)
  return if (index > size - 1)
    null
  else
    getCharFromBuffer(code, index)
}

fun nextCharacter(bundle: Bundle): Char? =
    nextCharacter(bundle.code, bundle.end.index)

fun singleCharacterTokenMatch(position: Position, character: Char): Response<TokenStep>? {
  val rune = singleCharacterTokens(character)
  return if (rune != null)
    success(TokenStep(
        token = Token(rune, Range(position, position), character.toString()),
        position = nextPosition(character, position)
    ))
  else
    null
}

fun tokenFromBundle(rune: Rune): (Bundle) -> Response<TokenStep> = { bundle ->
  success(TokenStep(
      token = Token(
          rune = rune,
          range = Range(bundle.start, bundle.end),
          text = bundle.buffer
      ),
      position = bundle.end
  ))
}

tailrec fun consumeSingleLineWhitespace(bundle: Bundle): Position {
  val character = consumeSingle(bundle, singleLineWhitespace)
  return if (character == null)
    bundle.end
  else
    consumeSingleLineWhitespace(incrementBundle(character, bundle))
}

tailrec fun consumeIdentifier(bundle: Bundle): Response<TokenStep> {
  val character = consumeSingle(bundle, identifierAfterStart)
  return if (character == null)
    tokenFromBundle(Rune.identifier)(bundle)
  else
    consumeIdentifier(incrementBundle(character, bundle))
}

tailrec fun consumeFloatAfterDot(bundle: Bundle): Response<TokenStep> {
  val character = consumeSingle(bundle, floatAfterDot)
  return if (character == null)
    tokenFromBundle(Rune.literalFloat)(bundle)
  else
    consumeFloatAfterDot(incrementBundle(character, bundle))
}

tailrec fun consumeInteger(bundle: Bundle): Response<TokenStep> {
  val character = nextCharacter(bundle)
  return if (character == dot)
    consumeFloatAfterDot(incrementBundle(character, bundle))
  else if (character == null || !integerAfterStart(character))
    tokenFromBundle(Rune.literalInteger)(bundle)
  else
    consumeInteger(incrementBundle(character, bundle))
}

typealias BundleToToken = (Bundle) -> Response<TokenStep>

fun branchTokenStart(character: Char): BundleToToken? =
    when {
      identifierStart(character) -> ::consumeIdentifier
      integerStart(character) -> ::consumeInteger
      else -> null
    }

fun tokenStart(code: CodeBuffer, position: Position, character: Char): Response<TokenStep> {
  val branch = branchTokenStart(character)
  return if (branch == null)
    failure(listOf(ParsingError(TextId.unexpectedCharacter, Range(position))))
  else
    branch(Bundle(code, nextPosition(character, position), buffer = newLexicalBuffer(character)))
}

fun tokenStart(code: CodeBuffer): (Position) -> Response<TokenStep> = { position ->
  val character = nextCharacter(code, position.index)
  if (character != null) {
    singleCharacterTokenMatch(position, character)
        ?: tokenStart(code, position, character)
  } else
    success(TokenStep(position))
}

fun nextToken(code: CodeBuffer, position: Position): Response<TokenStep> {
  return tokenStart(code)(consumeSingleLineWhitespace(Bundle(code, position)))
}

tailrec fun tokenize(code: CodeBuffer, position: Position, tokens: Tokens): Response<Tokens> {
  val result = nextToken(code, position)
  return when (result) {
    is Response.Failure -> failure(result.errors)
    is Response.Success -> {
      val (newPosition, token) = result.value
      if (token == null)
        success(tokens)
      else
        tokenize(code, newPosition, tokens.plus(token))
    }
  }
}

fun tokenize(code: CodeBuffer): Response<Tokens> =
    tokenize(code, position = newPosition(), tokens = listOf())
