package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId

val pushChild: ParsingStateTransition = { burg, state ->
  state.copy(
      burgStack = state.burgStack.plusElement(listOf(burg))
  )
}

val injectChild: ParsingStateTransition = { burg, state ->
  val intermediate = pushChild(burg, state)
  foldStack(intermediate)
}

val addSibling: ParsingStateTransition = { burg, state ->
  state.copy(
      burgStack = stackAppend(state.burgStack, burg)
  )
}

fun popChild(state: ParsingState): ParsingState {
  val stack = state.burgStack
  return state.copy(
      accumulator = state.accumulator + stack.last(),
      burgStack = stack.dropLast(1)
  )
}

fun popChildren(state: ParsingState): ParsingState {
  val stack = state.burgStack
  // Collapse the stack top and expand the range of the next stack's tail entry
  val shortStack = stack.dropLast(1)
  val children = stack.last()
  val newTop = shortStack.last()
  val parent = adoptChildren(newTop.last(), children)
  return popChild(state).copy(
      accumulator = state.accumulator + stack.last(),
      burgStack = stack.dropLast(2).plusElement(newTop.drop(1) + parent)
  )
}

val popChildren: ParsingStateTransition = { _, state ->
  popChildren(state)
}

fun foldStack(state: ParsingState): ParsingState =
    if (state.burgStack.size < 2)
      state
    else
      foldStack(popChildren(state))

val foldStack: ParsingStateTransition = { _, state ->
  foldStack(state)
}

val skip: ParsingStateTransition = { _, state ->
  state
}

fun addError(message: TextId): ParsingStateTransition = { burg, state ->
  state.copy(
      errors = state.errors + PendingParsingError(
          message = message,
          range = burg.range
      )
  )
}

fun parsingError(message: TextId) =
    ParsingStep(addError(message))

operator fun ParsingStateTransition.plus(other: ParsingStateTransition): ParsingStateTransition = { burg, state ->
  val intermediate = this(burg, state)
  other(burg, intermediate)
}
