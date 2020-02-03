package silentorb.imp.parsing.lexer

fun singleCharacterTokens(character: Char): Rune? =
    when (character) {
      '=' -> Rune.assignment
      '.' -> Rune.dot
      '(' -> Rune.parenthesisOpen
      ')' -> Rune.parenthesisClose
      else -> null
    }
