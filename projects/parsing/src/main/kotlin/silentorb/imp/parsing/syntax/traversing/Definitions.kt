package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseDefinitionName: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> definitionName
    else -> addError(TextId.expectedIdentifier)
  }
}

val parseDefinitionAssignment: TokenToParsingTransition = { token ->
  when {
    isAssignment(token) -> goto(ParsingMode.definitionParameterNameOrAssignment)
    else -> addError(TextId.expectedAssignment)
  }
}

val parseDefinitionParameterNameOrAssignment: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> startParameter
    isAssignment(token) -> startExpression + goto(ParsingMode.expressionStart)
    else -> addError(TextId.expectedParameterNameOrAssignment)
  }
}

val parseDefinitionParameterType: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) ->  parameterType
    else -> addError(TextId.expectedColon)
  }
}

val parseDefinitionParameterColon: TokenToParsingTransition = { token ->
  when {
    isColon(token) -> goto(ParsingMode.definitionParameterType)
    else -> addError(TextId.expectedColon)
  }
}

val parseBody: TokenToParsingTransition = { token ->
  when {
    isLet(token) -> startDefinition
    isNewline(token) || isEndOfFile(token) -> goto(ParsingMode.body)
    else -> addError(TextId.expectedLetKeyword)
  }
}
