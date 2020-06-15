package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.syntax.*

val startDefinition = ParsingStep(pushMarker(BurgType.definition), ParsingMode.definitionName)
val startImport = ParsingStep(pushMarker(BurgType.importClause), ParsingMode.importFirstPathToken)
val startGroup = ParsingStep(skip, ParsingMode.groupStart)
val startGroupNamedArgumentValue = ParsingStep(skip, ParsingMode.groupNamedArgumentValue)
val startArgument = foldTo(BurgType.application) + pushMarker(BurgType.argument) + pushMarker(BurgType.argumentValue)
val startGroupArgumentValue = startArgument + ParsingStep(skip, ParsingMode.groupStart)

val parameterName = ParsingStep(skip, ParsingMode.definitionParameterColon)
val startParameter = pushMarker(BurgType.parameter) + parameterName
val startExpression = push(BurgType.expression, asMarker) + parseRootExpressionStart

// Other
val nextDefinition = fold + startDefinition
val definitionName = ParsingStep(push(BurgType.definitionName, asString) + pop, ParsingMode.definitionParameterNameOrAssignment)
val firstImportPathToken = ParsingStep(push(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val followingImportPathToken = ParsingStep(append(BurgType.importPathToken, asString), ParsingMode.importSeparator)
val importPathWildcard = ParsingStep(append(BurgType.importPathWildcard, asString) + fold, ParsingMode.header)
val skipStep = ParsingStep(skip)
val descend = ParsingStep(skip, ParsingMode.descend, consume = false)
val startPipingRoot = ParsingStep(skip, ParsingMode.pipingRootStart)
val startPipingGroup = ParsingStep(skip, ParsingMode.pipingGroupedStart)
val closeImport = ParsingStep(fold, ParsingMode.header)

fun startSimpleApplication(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    pushMarker(BurgType.application) +
        pushMarker(BurgType.appliedFunction) +
        push(burgType, translator) +
        foldTo(BurgType.application)

val closeArgumentValue =
    foldTo(BurgType.application)

val closeArgumentName =
    ParsingStep(
        changeType(BurgType.argumentName) + removeParent + pop + pushMarker(BurgType.argumentValue),
        ParsingMode.expressionRootNamedArgumentValue)

fun applyPiping(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    foldTo(BurgType.application) +
        startSimpleApplication(burgType, translator) +
        flipTop +
        insertBelow(BurgType.argument, asMarker) +
        insertBelow(BurgType.argumentValue, asMarker) +
        foldTo(BurgType.application) +
        pop
