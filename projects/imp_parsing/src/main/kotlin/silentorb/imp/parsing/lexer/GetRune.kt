package silentorb.imp.parsing.lexer

fun getRune(mode: LexicalMode): Rune? =
    when (mode) {
      LexicalMode.identifier -> Rune.identifier
      else -> null
    }
