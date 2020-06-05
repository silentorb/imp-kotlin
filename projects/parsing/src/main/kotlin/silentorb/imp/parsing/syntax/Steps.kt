package silentorb.imp.parsing.syntax

val startDefinition = ParsingStep(push(BurgType.definition, asMarker), ParsingMode.definitionName)

val definitionName = ParsingStep(push(BurgType.definitionName, asString) + pop, ParsingMode.definitionParameterNameOrAssignment)

val firstImportPathToken = ParsingStep(push(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val followingImportPathToken = ParsingStep(append(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val importPathWildcard = ParsingStep(push(BurgType.importPathToken, asString), ParsingMode.header)

val startSubExpression = ParsingStep(push(BurgType.expression, asMarker), ParsingMode.subExpression)
