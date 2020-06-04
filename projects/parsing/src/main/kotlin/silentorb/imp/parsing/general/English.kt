package silentorb.imp.parsing.general

fun englishText(text: Any): String =
    when (text as TextId) {
      TextId.ambiguousOverload -> "Ambiguous overload"
      TextId.badArgument -> "Bad argument"
      TextId.circularDependency -> "Circular dependency"
      TextId.duplicateSymbol -> "Duplicate symbol @tokenText"
      TextId.expectedAssignment -> "Expected an equals sign"
      TextId.expectedColon -> "Expected a colon"
      TextId.expectedCommaOrAssignment -> "Expected a comma or equals sign"
      TextId.expectedExpression -> "Expected an expression"
      TextId.expectedIdentifier -> "Expected an identifier"
      TextId.expectedIdentifierOrWildcard -> "Expected an identifier or asterisk"
      TextId.expectedImportOrLetKeywords -> "Expected \"import\" or \"let\""
      TextId.expectedLetKeyword -> "Expected \"let\""
      TextId.expectedNewline -> "Expected a new line"
      TextId.expectedParameterName -> "Expected a parameter name"
      TextId.expectedParameterNameOrAssignment -> "Expected a parameter name or equals sign"
      TextId.expectedPeriodOrNewline -> "Expected a period or new line"
      TextId.importNotFound -> "Import not found"
      TextId.incompleteParameter -> "Missing parameter type"
      TextId.invalidToken -> "Invalid symbol"
      TextId.missingArgumentName -> "Missing argument name"
      TextId.missingArgumentExpression -> "Missing argument expression"
      TextId.missingClosingParenthesis -> "Missing closing parentheses"
      TextId.missingExpression -> "Missing expression"
      TextId.multipleGraphOutputs -> "Multiple graph outputs"
      TextId.missingImportPath -> "Missing import path"
      TextId.noGraphOutput -> "No graph output"
      TextId.noMatchingSignature -> "No matching function signature for (%s)"
      TextId.outsideTypeRange -> "Outside type range"
      TextId.unexpectedCharacter -> "Unexpected character"
      TextId.unknownFunction -> "Unknown function"

//      // TODO: Temporarily adding an else because message rendering isn't being used yet and it will be easier to update this function in a batches
//      else -> throw Error("Not implemented: $text")
    }
