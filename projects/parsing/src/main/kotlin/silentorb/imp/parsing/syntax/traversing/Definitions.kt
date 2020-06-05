package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseDefinitionName: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> definitionName
    else -> parsingError(TextId.expectedIdentifier)
  }
}

val parseDefinitionAssignment: TokenToParsingTransition = { token ->
  when {
    isAssignment(token) -> ParsingStep(skip, ParsingMode.definitionParameterNameOrAssignment)
    else -> parsingError(TextId.expectedAssignment)
  }
}

val parseDefinitionParameterName: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(skip, ParsingMode.definitionParameterColon)
    else -> parsingError(TextId.expectedParameterName)
  }
}

val parseDefinitionParameterNameOrAssignment: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(skip, ParsingMode.definitionParameterColon)
    isAssignment(token) -> ParsingStep(skip, ParsingMode.expression)
    else -> parsingError(TextId.expectedParameterNameOrAssignment)
  }
}

val parseDefinitionParameterType = parseType(ParsingMode.definitionParameterSeparatorOrAssignment)

val parseDefinitionParameterColon: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> ParsingStep(skip, ParsingMode.definitionParameterType)
    else -> parsingError(TextId.expectedColon)
  }
}

val parseDefinitionParameterSeparatorOrAssignment: TokenToParsingTransition = { token ->
  when {
    isComma(token) -> ParsingStep(skip, ParsingMode.definitionParameterName)
    isAssignment(token) -> ParsingStep(skip, ParsingMode.expression)
    else -> parsingError(TextId.expectedCommaOrAssignment)
  }
}

val parseBody: TokenToParsingTransition = { token ->
  when {
    isLet(token) -> startDefinition
    isNewline(token) -> ParsingStep(skip, ParsingMode.body)
    else -> parsingError(TextId.expectedLetKeyword)
  }
}

val parseDefinitionExpression = parseExpressionStart(ParsingMode.body)
