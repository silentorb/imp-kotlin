package silentorb.imp.parsing.general

fun englishText(text: TextId): String =
    when(text) {
      TextId.duplicateSymbol -> "Duplicate symbol @tokenText"
      TextId.unexpectedCharacter -> "Unexpected character"
      TextId.expectedIdentifier -> "Expected an identifier"
      TextId.expectedExpression -> "Expected an expression"
      TextId.expectedNewline -> "Expected a new line"
    }

