package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId

fun pushChild(type: BurgType): ParsingStateTransition = { token, state ->
  val burg = PendingBurg(
      type = type,
      range = token.range,
      value = token.value
  )
  state.copy(
//      burgs = state.burgs + (burg.hashCode() to burg),
//      roads = state.roads + (burg.hashCode() to state.stack.last()),
      burgStack = state.burgStack.plusElement(listOf(burg))
  )
}

fun addSibling(type: BurgType): ParsingStateTransition = { token, state ->
  val burg = PendingBurg(
      type = type,
      range = token.range,
      value = token.value
  )
  state.copy(
      burgStack = stackAppend(state.burgStack, burg)
  )
}

fun popChild(state: ParsingState): ParsingState {
  val stack = state.burgStack
  return state.copy(
      burgs = state.burgs + stack.last(),
      burgStack = stack.dropLast(1)
  )
}

fun adoptChildren(parent: PendingBurg, children: List<PendingBurg>) =
    parent.copy(
        range = parent.range.copy(
            end = children.last().range.end
        ),
        children = children.map { it.hashCode() }
    )

fun popChildren(state: ParsingState): ParsingState {
  val stack = state.burgStack
  // Collapse the stack top and expand the range of the next stack's tail entry
  val newTop = stack.dropLast(1).last()
  val parent = adoptChildren(newTop.last(), stack.last())
  return popChild(state).copy(
      burgs = state.burgs + stack.last(),
      burgStack = stack.dropLast(2).plusElement(newTop.drop(1) + parent)
  )
}

val popChildren: ParsingStateTransition = { _, state ->
  popChildren(state)
}

fun resetStack(state: ParsingState): ParsingState =
    if (state.burgStack.size < 2)
      state
    else
      resetStack(popChildren(state))

val foldStack: ParsingStateTransition = { _, state ->
  resetStack(state)
}

val skip: ParsingStateTransition = { _, state ->
  state
}

fun addError(message: TextId): ParsingStateTransition = { token, state ->
  state.copy(
      errors = state.errors + PendingParsingError(
          message = message,
          range = token.range
      )
  )
}

fun parsingError(message: TextId) = null to addError(message)
