package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.syntax.*

val startDefinition = ParsingStep(pushMarker(BurgType.definition), ParsingMode.definitionName)
val startImport = ParsingStep(pushMarker(BurgType.importClause), ParsingMode.importFirstPathToken)
val startGroup = ParsingStep(pushMarker(BurgType.group), ParsingMode.groupStart)

val parameterName = ParsingStep(skip, ParsingMode.definitionParameterColon)
val startParameter = pushMarker(BurgType.parameter) + parameterName

val startApplication = pushMarker(BurgType.parameter)
val startArgument = pushMarker(BurgType.argument)

// Other
val nextDefinition = fold + startDefinition
val definitionName = ParsingStep(push(BurgType.definitionName, asString) + pop, ParsingMode.definitionParameterNameOrAssignment)
val firstImportPathToken = ParsingStep(push(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val followingImportPathToken = ParsingStep(append(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val importPathWildcard = ParsingStep(append(BurgType.importPathWildcard, asString) + fold, ParsingMode.header)
val skipStep = ParsingStep(skip)
val descend = ParsingStep(skip, ParsingMode.descend, consume = false)
