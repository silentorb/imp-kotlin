package silentorb.imp.parsing.general

fun englishText(text: TextId): String =
    when(text) {
      TextId.unexpectedCharacter -> "Unexpected character at @position"
      TextId.expectedIdentifier -> "Expected an identifier at @position"
      TextId.expectedExpression -> "Expected an expression at @position"
      TextId.expectedNewline -> "Expected a new line at @position"
    }

