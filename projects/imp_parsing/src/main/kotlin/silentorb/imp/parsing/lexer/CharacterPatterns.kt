package silentorb.imp.parsing.lexer

val identifierStart = Regex("[a-zA-Z_]")
val identifierPastStart = Regex("[a-zA-Z_0-9]")

val integerStart = Regex("[1-9]")
val integerPastStart = Regex("[0-9]")

typealias CharacterLexer = (Char) -> LexicalMode
typealias OptionalCharacterLexer = (Char) -> LexicalMode?

fun onFresh(character: Char): LexicalMode? {
  val string = character.toString()
  return when {
    string.matches(identifierStart) -> LexicalMode.identifier
    string.matches(integerStart) -> LexicalMode.integer
    character == ' ' -> LexicalMode.fresh
    else -> null
  }
}

fun onIdentifier(character: Char): LexicalMode {
  val string = character.toString()
  return if (string.matches(identifierPastStart))
    LexicalMode.identifier
  else
    LexicalMode.fresh
}

fun onInteger(character: Char): LexicalMode {
  val string = character.toString()
  return if (string.matches(integerPastStart))
    LexicalMode.integer
  else
    LexicalMode.fresh
}

fun processChar(character: Char, mode: LexicalMode): LexicalMode? {
  val next: OptionalCharacterLexer = when (mode) {
    LexicalMode.fresh -> ::onFresh
    LexicalMode.identifier -> ::onIdentifier
    LexicalMode.integer -> ::onInteger
  }

  return next(character)
}
