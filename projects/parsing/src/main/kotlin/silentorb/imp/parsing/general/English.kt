package silentorb.imp.parsing.general

fun englishText(text: TextId): String =
    when (text) {
      TextId.ambiguousOverload -> "Ambiguous overload"
      TextId.duplicateSymbol -> "Duplicate symbol @tokenText"
      TextId.expectedExpression -> "Expected an expression"
      TextId.expectedIdentifier -> "Expected an identifier"
      TextId.expectedNewline -> "Expected a new line"
      TextId.importNotFound -> "Import not found"
      TextId.invalidToken -> "Invalid symbol"
      TextId.missingClosingParenthesis -> "Missing closing parentheses"
      TextId.missingExpression -> "Missing expression"
      TextId.multipleGraphOutputs -> "Multiple graph outputs"
      TextId.noGraphOutput -> "No graph output"
      TextId.noMatchingSignature -> "No matching function signature"
      TextId.unexpectedCharacter -> "Unexpected character"
      TextId.unknownFunction -> "Unknown function"

      // TODO: Temporarily adding an else because message rendering isn't being used yet and it will be easier to update this function in a batches
      else -> throw Error("Not implemented")
    }
