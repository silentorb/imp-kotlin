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

  return if (shortStack.none())
    state // An error occurred.  Should be flagged by other code.
  else {
    val newTop = shortStack.last()
    val parent = adoptChildren(state.accumulator, newTop.last(), children)
    if (parent.type == BurgType.argument) {
      val k = 0
    }
    return state.copy(
      accumulator = state.accumulator + children,
      burgStack = stack.dropLast(2).plusElement(newTop.drop(1) + parent)
    )
  }
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

fun foldToInclusive(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  if (state.burgStack.size < 2 || state.burgStack.last().first().type == burgType) {
    val stack = state.burgStack
    val head = stack.last().last()
    val newRange = head.range.copy(
        end = head.range.end.copy(
            index = head.range.end.index + newBurg(BurgType.block, asString).range.length
        )
    )
    val newHead = head.copy(range = newRange)
    val newTop = stack.last().dropLast(1).plus(newHead)
    state.copy(
        burgStack = stack.dropLast(1).plusElement(newTop)
    )
  } else
    foldToInclusive(burgType)(newBurg, popChildren(state))
}

val skipOld: ParsingStateTransition = { _, state ->
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

val popContextMode: ParsingStateTransition = { _, state ->
  state.copy(
      contextStack = state.contextStack.dropLast(1)
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

operator fun ParsingStateTransition.plus(other: ParsingStateTransition): ParsingStateTransition = { newBurg, state ->
  val intermediate = this(newBurg, state)
  other(newBurg, intermediate)
}

operator fun ParsingStateTransition.plus(other: TokenToParsingTransition): TokenToParsingTransition = { token ->
  this + other(token)
}
