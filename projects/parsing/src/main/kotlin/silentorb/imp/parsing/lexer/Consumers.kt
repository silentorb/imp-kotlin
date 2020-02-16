package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.*

tailrec fun consumeSingleLineWhitespace(bundle: Bundle): Position {
  val character = consumeSingle(bundle, singleLineWhitespace)
  return if (character == null)
    bundle.end
  else
    consumeSingleLineWhitespace(incrementBundle(character, bundle))
}

// Multiple newlines in a row are grouped together as a single token
tailrec fun consumeNewline(bundle: Bundle): Response<TokenStep> {
  val character = consumeSingle(bundle, newLineAfterStart)
  return if (character == null)
    tokenFromBundle(Rune.newline)(bundle)
  else
    consumeNewline(incrementBundle(character, bundle))
}

tailrec fun consumeIdentifier(bundle: Bundle): Response<TokenStep> {
  val character = consumeSingle(bundle, identifierAfterStart)
  return if (character == null)
    tokenFromBundle(Rune.identifier)(bundle)
  else
    consumeIdentifier(incrementBundle(character, bundle))
}

tailrec fun consumeOperator(bundle: Bundle): Response<TokenStep> {
  val character = consumeSingle(bundle, operator)
  return if (character == null)
    tokenFromBundle(Rune.operator)(bundle)
  else
    consumeOperator(incrementBundle(character, bundle))
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

fun consumeLiteralZero(bundle: Bundle): Response<TokenStep> {
  val character = nextCharacter(bundle)
  return if (character == dot)
    consumeFloatAfterDot(incrementBundle(character, bundle))
  else if (character != null && integerAfterStart(character))
    failure(newParsingError(TextId.unexpectedCharacter, Range(bundle.start)))
  else
    tokenFromBundle(Rune.literalInteger)(bundle)
}
