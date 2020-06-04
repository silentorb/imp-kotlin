package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.lexer.Rune

val parseDefinitionName: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier -> ParsingMode.definitionParameterNameOrAssignment to skip
    else -> parsingError(TextId.expectedIdentifier)
  }
}

val parseDefinitionAssignment: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.operator && token.value == "=" -> ParsingMode.definitionParameterNameOrAssignment to skip
    else -> parsingError(TextId.expectedAssignment)
  }
}

val parseDefinitionParameterName: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier -> ParsingMode.definitionParameterColon to skip
    else -> parsingError(TextId.expectedParameterName)
  }
}

val parseDefinitionParameterNameOrAssignment: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier -> ParsingMode.definitionParameterColon to skip
    else -> parsingError(TextId.expectedParameterNameOrAssignment)
  }
}

val parseDefinitionParameterType = parseType(ParsingMode.definitionParameterSeparatorOrAssignment)

val parseDefinitionParameterColon: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier -> ParsingMode.definitionParameterType to skip
    else -> null to addError(TextId.expectedColon)
  }
}

val parseDefinitionParameterSeparatorOrAssignment: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.comma -> ParsingMode.definitionParameterName to skip
    token.rune == Rune.operator && token.value == "=" -> ParsingMode.definitionExpression to skip
    else -> parsingError(TextId.expectedCommaOrAssignment)
  }
}

val parseBody: TokenToParsingTransition = { token ->
  when {
    token.rune == Rune.identifier && token.value == "let" -> ParsingMode.definitionName to skip
    token.rune == Rune.newline -> ParsingMode.body to foldStack
    else -> parsingError(TextId.expectedLetKeyword)
  }
}

val parseDefinitionExpression = parseExpression(ParsingMode.body)
