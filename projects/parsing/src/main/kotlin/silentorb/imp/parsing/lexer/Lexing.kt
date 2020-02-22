package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.*

fun nextCharacter(code: CodeBuffer, index: CodeInt): Char? {
  val size = getCodeBufferSize(code)
  return if (index > size - 1)
    null
  else
    getCharFromBuffer(code, index)
}

fun nextCharacter(bundle: Bundle): Char? =
    nextCharacter(bundle.code, bundle.end.index)

fun singleCharacterTokenMatch(position: Position, character: Char): TokenStep? {
  val rune = singleCharacterTokens(character)
  return if (rune != null)
    TokenStep(
        token = Token(rune, Range(position, position), character.toString()),
        position = nextPosition(character, position)
    )
  else
    null
}

typealias BundleToToken = (Bundle) -> TokenStep

fun branchTokenStart(character: Char): BundleToToken? =
    when {
      literalZero(character) -> ::consumeLiteralZero
      newLineStart(character) -> ::consumeNewline
      identifierStart(character) -> ::consumeIdentifier
      integerStart(character) -> ::consumeInteger
      operatorStart(character) -> ::consumeOperator
      commentStartOrHyphen(character) -> ::consumeCommentOrHyphen
      else -> null
    }

fun tokenStart(code: CodeBuffer, position: Position, character: Char): TokenStep {
  val branch = branchTokenStart(character)
  return if (branch == null)
    badCharacter(Range(position, nextPosition(character, position)))
  else
    branch(Bundle(
        code = code,
        start = position,
        end = nextPosition(character, position),
        buffer = newLexicalBuffer(character)
    ))
}

fun tokenStart(code: CodeBuffer): (Position) -> TokenStep = { position ->
  val character = nextCharacter(code, position.index)
  if (character != null) {
    singleCharacterTokenMatch(position, character)
        ?: tokenStart(code, position, character)
  } else
    TokenStep(position)
}

fun nextToken(code: CodeBuffer, position: Position): TokenStep {
  return tokenStart(code)(consumeSingleLineWhitespace(Bundle(code, position, position)))
}

tailrec fun tokenize(code: CodeBuffer, position: Position, tokens: Tokens): Tokens {
  val result = nextToken(code, position)
  val (newPosition, token) = result
  return if (token == null)
    tokens
  else
    tokenize(code, newPosition, tokens.plus(token))
}

fun tokenize(code: CodeBuffer): Tokens =
    tokenize(code, position = newPosition(), tokens = listOf())
