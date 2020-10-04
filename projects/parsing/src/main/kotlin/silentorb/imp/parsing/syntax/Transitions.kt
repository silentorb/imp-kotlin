package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.traversing.closeBlock
import silentorb.imp.parsing.syntax.traversing.closeGroup
import silentorb.imp.parsing.syntax.traversing.closeRedundantGroup
import silentorb.imp.parsing.syntax.traversing.nextDefinition

val asString: ValueTranslator = { it }
val asFloat: ValueTranslator = { it.toFloat() }
val asInt: ValueTranslator = { it.toInt() }

fun push(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = state.burgStack + BurgLayer(burgs = listOf(newBurg(burgType, valueTranslator)))
  )
}

fun push(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = state.burgStack + BurgLayer(type = burgType)
  )
}

fun changeType(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  val stack = state.burgStack
  val previous = stack.last().burgs.last()
  val next = previous.copy(
      type = burgType
  )
  state.copy(
      burgStack = stack.dropLast(1) + BurgLayer(burgs = listOf(next))
  )
}

fun append(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = stackAppend(state.burgStack, newBurg(burgType, valueTranslator))
  )
}

val pop: ParsingStateTransition = { _, state ->
  popChildren(state)
}

val removeParent: ParsingStateTransition = { _, state ->
  val stack = state.burgStack
  state.copy(
      burgStack = stack.dropLast(2).plusElement(stack.last())
  )
}

val flipTop: ParsingStateTransition = { _, state ->
  val stack = state.burgStack
  val next = state.copy(
      burgStack = stack
          .dropLast(2)
          .plusElement(stack.last())
          .plusElement(stack.dropLast(1).last())
  )
  next
}

fun insertBelow(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  val stack = state.burgStack
  state.copy(
      burgStack = stack
          .dropLast(1)
          + BurgLayer(burgs = listOf(newBurg(burgType, valueTranslator)))
          + stack.last()
  )
}

fun fold(state: ParsingState): ParsingState =
    if (state.burgStack.size < 2)
      state
    else
      fold(popChildren(state))

val fold: ParsingStateTransition = { _, state ->
  fold(state)
}

fun foldTo(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  if (state.burgStack.size < 2 || state.burgStack.last().type == burgType)
    state
  else
    foldTo(burgType)(newBurg, popChildren(state))
}

val skip: ParsingStateTransition = { _, state ->
  state
}

fun goto(mode: ParsingMode): ParsingStep = { _, state ->
  state.copy(
      mode = mode
  )
}

fun pushContextMode(contextMode: ContextMode): ParsingStateTransition = { _, state ->
  state.copy(
      contextStack = state.contextStack + contextMode
  )
}

val pushGroupStart: ParsingStateTransition = { _, state ->
  val contextMode = if (state.mode == ParsingMode.expressionArgumentStart ||
      state.mode == ParsingMode.expressionArgumentFollowing ||
      state.mode == ParsingMode.expressionNamedArgumentValue)
    ContextMode.group
  else
    ContextMode.redundantGroup

  state.copy(
      contextStack = state.contextStack + contextMode
  )
}

val popContextMode: ParsingStateTransition = { _, state ->
  state.copy(
      contextStack = state.contextStack.dropLast(1)
  )
}

fun addError(message: TextId): ParsingStateTransition {
  return { newBurg, state ->
    state.copy(
        errors = state.errors + PendingParsingError(
            message = message,
            range = newBurg(BurgType.bad) { null }.range
        )
    )
  }
}

operator fun ParsingStateTransition.plus(other: ParsingStateTransition): ParsingStateTransition = { newBurg, state ->
  val intermediate = this(newBurg, state)
  other(newBurg, intermediate)
}

operator fun ParsingStateTransition.plus(other: TokenToParsingTransition): TokenToParsingTransition = { token ->
  this + other(token)
}

val tryCloseGroup: ParsingStateTransition = { newBurg, state ->
  when {
    state.contextStack.lastOrNull() == ContextMode.group -> closeGroup(newBurg, state)
    state.contextStack.lastOrNull() == ContextMode.redundantGroup -> closeRedundantGroup(newBurg, state)
    else -> addError(TextId.missingOpeningParenthesis)(newBurg, state)
  }
}

val tryCloseBlock: ParsingStateTransition = { newBurg, state ->
  if (state.contextStack.lastOrNull() == ContextMode.block)
    closeBlock(newBurg, state)
  else
    addError(TextId.invalidToken)(newBurg, state)
}

val checkGroupClosed: ParsingStateTransition = { newBurg, state ->
  when (state.contextStack.lastOrNull()) {
    ContextMode.group, ContextMode.redundantGroup -> addError(TextId.missingClosingParenthesis)(newBurg, state)
    else -> skip(newBurg, state)
  }
}

val tryNextDefinition: ParsingStateTransition = { newBurg, state ->
  when (state.contextStack.lastOrNull()) {
    ContextMode.group, ContextMode.redundantGroup ->
      (addError(TextId.missingClosingParenthesis) + nextDefinition)(newBurg, state)

    else -> nextDefinition(newBurg, state)
  }
}
