package silentorb.imp.parsing.lexer

fun singleCharacterTokens(character: Char): Rune? =
    when (character) {
      '=' -> Rune.assignment
      ':' -> Rune.colon
      ',' -> Rune.comma
      '.' -> Rune.dot
      '(' -> Rune.parenthesesOpen
      ')' -> Rune.parenthesesClose
      else -> null
    }
