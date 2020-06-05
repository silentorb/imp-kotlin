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

fun append(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = stackAppend(state.burgStack, newBurg(burgType, valueTranslator))
  )
}

fun popChildren(state: ParsingState): ParsingState {
  val stack = state.burgStack
  val shortStack = stack.dropLast(1)
  val children = stack.last()
  val newTop = shortStack.last()
  val parent = adoptChildren(newTop.last(), children)
  return state.copy(
      accumulator = state.accumulator + children,
      burgStack = stack.dropLast(2).plusElement(newTop.drop(1) + parent)
  )
}

val pop: ParsingStateTransition = { _, state ->
  popChildren(state)
}

fun fold(state: ParsingState): ParsingState =
    if (state.burgStack.size < 2)
      state
    else
      fold(popChildren(state))

val fold: ParsingStateTransition = { _, state ->
  fold(state)
}

val skip: ParsingStateTransition = { _, state ->
  state
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