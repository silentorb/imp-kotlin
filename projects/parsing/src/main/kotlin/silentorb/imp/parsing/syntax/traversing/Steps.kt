package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.syntax.*

// Markers with starting symbols
val startDefinition = ParsingStep(push(BurgType.definition, asMarker), ParsingMode.definitionName)
val startImport = ParsingStep(push(BurgType.importClause, asMarker), ParsingMode.importFirstPathToken)
val startSubExpression = ParsingStep(push(BurgType.group, asMarker), ParsingMode.subExpressionStart)

// Markers without starting symbols
val startExpression = parsePushMarker(BurgType.expression, ParsingMode.expressionRootStart)
val startParameter = ParsingStep(push(BurgType.parameter, asMarker), ParsingMode.definitionParameterName)

// Other
val nextDefinition = fold + startDefinition
val definitionName = ParsingStep(push(BurgType.definitionName, asString) + pop, ParsingMode.definitionParameterNameOrAssignment)
val firstImportPathToken = ParsingStep(push(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val followingImportPathToken = ParsingStep(append(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val importPathWildcard = ParsingStep(append(BurgType.importPathWildcard, asString) + fold, ParsingMode.header)
val skipStep = ParsingStep(skip)
val descend = ParsingStep(skip, ParsingMode.descend, consume = false)
