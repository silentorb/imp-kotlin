package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val startDefinition = pushMarker(BurgType.definition) + goto(ParsingMode.definitionName)
val startImport = pushMarker(BurgType.importClause) + goto(ParsingMode.importFirstPathToken)
val startGroup = pushContextMode(ContextMode.group)
val closeGroup = foldTo(BurgType.application) + pop + popContextMode

//val startGroupNamedArgumentValue = goto(ParsingMode.groupNamedArgumentValue)
val startArgument = foldTo(BurgType.application) + pushMarker(BurgType.argument) + pushMarker(BurgType.argumentValue)
val startGroupArgumentValue = startArgument + startGroup + goto(ParsingMode.expressionStart)
val startBlock = pushContextMode(ContextMode.block)

val startParameter =
    pushMarker(BurgType.parameter) +
        push(BurgType.parameterName, asString) +
        pop +
        goto(ParsingMode.definitionParameterColon)

val parameterType =
    push(BurgType.parameterType, asString) +
        pop +
        pop +
        goto(ParsingMode.definitionParameterNameOrAssignment)

val startExpression = push(BurgType.expression, asMarker)

// Other
val nextDefinition = fold + startDefinition
val definitionName = push(BurgType.definitionName, asString) + pop + goto(ParsingMode.definitionParameterNameOrAssignment)
val firstImportPathToken = push(BurgType.importPathToken, asString) + goto(ParsingMode.importSeparator)
val followingImportPathToken = append(BurgType.importPathToken, asString) + goto(ParsingMode.importSeparator)
val importPathWildcard = append(BurgType.importPathWildcard, asString) + fold + goto(ParsingMode.header)

val startPipingRoot = goto(ParsingMode.pipingRootStart)

//val startPipingGroup = goto(ParsingMode.pipingGroupedStart)
val closeImport = fold + goto(ParsingMode.header)

fun startSimpleApplication(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    pushMarker(BurgType.application) +
        pushMarker(BurgType.appliedFunction) +
        push(burgType, translator) +
        foldTo(BurgType.application)

val closeArgumentValue =
    foldTo(BurgType.application)

val closeArgumentName =
    changeType(BurgType.argumentName) + removeParent + pop + pushMarker(BurgType.argumentValue) +
        goto(ParsingMode.expressionNamedArgumentValue)

fun applyPiping(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    foldTo(BurgType.application) +
        startSimpleApplication(burgType, translator) +
        flipTop +
        insertBelow(BurgType.argument, asMarker) +
        insertBelow(BurgType.argumentValue, asMarker) +
        foldTo(BurgType.application) +
        pop

fun checkGroupClosed(contextMode: ContextMode) =
    if (contextMode == ContextMode.group)
      addError(TextId.missingClosingParenthesis)
    else
      skip

fun tryCloseGroup(contextMode: ContextMode) =
    if (contextMode == ContextMode.group)
      closeGroup
    else
      addError(TextId.missingOpeningParenthesis)
