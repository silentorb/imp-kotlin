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

val popAppend: ParsingStateTransition = { _, state ->
  val stack = state.burgStack
  val shortStack = stack.dropLast(1)
  val children = stack.last()
  val newTop = shortStack.last()
  state.copy(
      burgStack = stack.dropLast(2).plusElement(newTop + children)
  )
}

val liftParent: ParsingStateTransition = { _, state ->
  val stack = state.burgStack
  val shortStack = stack.dropLast(1)
  val upperLayer = stack.last()
  val lowerLayer = shortStack.last()
  val oldParent = lowerLayer.last()
  assert(upperLayer.size == 1)
  val oldReplacementParent = upperLayer.first()
  val argumentValue = Burg(
      type = BurgType.argument,
      range = oldParent.range,
      file = oldParent.file,
      children = listOf(oldParent.hashCode()),
      value = null
  )
  val newArgument = Burg(
      type = BurgType.argument,
      range = oldParent.range,
      file = oldParent.file,
      children = listOf(argumentValue.hashCode()),
      value = null
  )
  val newReplacementParent = upperLayer.first()
      .copy(
          range = oldReplacementParent.range.copy(
              start = oldParent.range.start
          ),
          children = oldReplacementParent.children + newArgument.hashCode()
      )
  state.copy(
      accumulator = state.accumulator - oldParent + upperLayer + argumentValue + newArgument,
      burgStack = stack.dropLast(2).plusElement(lowerLayer.drop(1) + newReplacementParent)
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
