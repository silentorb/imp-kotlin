package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId

val asMarker: ValueTranslator = { null }
val asString: ValueTranslator = { it }
val asFloat: ValueTranslator = { it.toFloat() }
val asInt: ValueTranslator = { it.toInt() }

fun push(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = state.burgStack.plusElement(listOf(newBurg(burgType, valueTranslator)))
  )
}

fun changeType(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  val stack = state.burgStack
  val previous = stack.last().last()
  val next = previous.copy(
      type = burgType
  )
  state.copy(
      burgStack = stack.dropLast(1).plusElement(listOf(next))
  )
}

fun pushMarker(burgType: BurgType) =
    push(burgType, asMarker)

val pushEmpty: ParsingStateTransition = { _, state ->
  state.copy(
      burgStack = state.burgStack.plusElement(listOf())
  )
}

fun append(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = stackAppend(state.burgStack, newBurg(burgType, valueTranslator))
  )
}

fun popChildren(state: ParsingState): ParsingState {
  val stack = state.burgStack
  val shortStack = stack.dropLast(1)
  val children = stack.last()
      .filter { it.children.any() || it.value != null } // Empty children are filtered out

  val newTop = shortStack.last()
  val parent = adoptChildren(newTop.last(), children)
  if (parent.type == BurgType.argument) {
    val k = 0
  }
  return state.copy(
      accumulator = state.accumulator + children,
      burgStack = stack.dropLast(2).plusElement(newTop.drop(1) + parent)
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

val popAppend: ParsingStateTransition = { _, state ->
  val stack = state.burgStack
  val shortStack = stack.dropLast(1)
  val children = stack.last()
  val newTop = shortStack.last()
  state.copy(
      burgStack = stack.dropLast(2).plusElement(newTop + children)
  )
}

fun insertBelow(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  val stack = state.burgStack
  state.copy(
      burgStack = stack
          .dropLast(1)
          .plusElement(listOf(newBurg(burgType, valueTranslator)))
          .plusElement(stack.last())
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
  if (state.burgStack.size < 2 || state.burgStack.last().first().type == burgType)
    state
  else
    foldTo(burgType)(newBurg, popChildren(state))
}

val skip: ParsingStateTransition = { _, state ->
  state
}

fun onReturn(mode: ParsingMode): ParsingStateTransition = { _, state ->
  state.copy(
      modeStack = state.modeStack + mode
  )
}

fun addError(message: TextId): ParsingStateTransition = { newBurg, state ->
  state.copy(
      errors = state.errors + PendingParsingError(
          message = message,
          range = newBurg(BurgType.bad, asMarker).range
      )
  )
}

fun parsingError(message: TextId) =
    ParsingStep(addError(message))

operator fun ParsingStateTransition.plus(other: ParsingStateTransition): ParsingStateTransition = { newBurg, state ->
  val intermediate = this(newBurg, state)
  other(newBurg, intermediate)
}

operator fun ParsingStateTransition.plus(other: ParsingStep): ParsingStep =
    other.copy(
        transition = this + other.transition
    )

operator fun ParsingStateTransition.plus(other: TokenToParsingTransition): TokenToParsingTransition = { token ->
  this + other(token)
}
