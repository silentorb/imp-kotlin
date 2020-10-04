package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.syntax.*

val startDefinition = push(BurgType.definition) + goto(ParsingMode.definitionName)
val startImport = push(BurgType.importClause) + goto(ParsingMode.importFirstPathToken)
val startGroup = pushGroupStart

val closeGroup = foldTo(BurgType.application) + pop + popContextMode

val closeRedundantGroup = popContextMode

val startArgument =
    foldTo(BurgType.application) +
        push(BurgType.argument) +
        push(BurgType.argumentValue)

val startGroupArgumentValue = startArgument + startGroup + goto(ParsingMode.expressionStart)
val startBlock = pushContextMode(ContextMode.block) + push(BurgType.block) + goto(ParsingMode.block)
val closeBlock = foldTo(BurgType.block) + pop + foldTo(BurgType.block) + popContextMode + goto(ParsingMode.block)

val startParameter =
    push(BurgType.parameter) +
        push(BurgType.parameterName, asString) +
        pop +
        goto(ParsingMode.definitionParameterColon)

val parameterType =
    push(BurgType.parameterType, asString) +
        pop +
        pop +
        goto(ParsingMode.definitionParameterNameOrAssignment)

val nextDefinition = foldTo(BurgType.block) + startDefinition
val definitionName = push(BurgType.definitionName, asString) + pop + goto(ParsingMode.definitionParameterNameOrAssignment)
val firstImportPathToken = push(BurgType.importPathToken, asString) + goto(ParsingMode.importSeparator)
val followingImportPathToken = append(BurgType.importPathToken, asString) + goto(ParsingMode.importSeparator)
val importPathWildcard = append(BurgType.importPathWildcard, asString) + fold + goto(ParsingMode.header)

val startPipingRoot = goto(ParsingMode.pipingRootStart)

val closeImport = fold + goto(ParsingMode.header)

val startSimpleApplication = push(BurgType.application)

fun startApplication(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    push(BurgType.application) +
        push(burgType, translator) +
        foldTo(BurgType.application)

val closeArgumentValue =
    foldTo(BurgType.application)

val closeArgumentName =
    changeType(BurgType.argumentName) + removeParent + pop + push(BurgType.argumentValue) +
        goto(ParsingMode.expressionNamedArgumentValue)

fun applyPiping(burgType: BurgType, translator: ValueTranslator): ParsingStateTransition =
    foldTo(BurgType.application) +
        startApplication(burgType, translator) +
        flipTop +
        insertBelow(BurgType.argument) { null } +
        insertBelow(BurgType.argumentValue) { null } +
        foldTo(BurgType.application) +
        pop
