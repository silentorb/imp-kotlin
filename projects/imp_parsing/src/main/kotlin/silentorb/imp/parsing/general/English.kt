package silentorb.imp.parsing.general

fun englishText(text: TextId): String =
    when(text) {
      TextId.unexpectedCharacter -> "Unexpected character at @position"
    }

