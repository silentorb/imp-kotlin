package silentorb.imp.parsing.lexer

fun singleCharacterTokens(character: Char): Rune? =
    when (character) {
      '=' -> Rune.assignment
      ':' -> Rune.colon
      ',' -> Rune.comma
      '.' -> Rune.dot
      '(' -> Rune.parenthesesOpen
      ')' -> Rune.parenthesesClose
      '{' -> Rune.braceOpen
      '}' -> Rune.braceClose
      else -> null
    }
