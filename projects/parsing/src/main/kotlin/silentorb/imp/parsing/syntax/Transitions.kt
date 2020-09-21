package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.lexer.parentheses
import silentorb.imp.parsing.syntax.traversing.closeBlock
import silentorb.imp.parsing.syntax.traversing.closeGroup
import silentorb.imp.parsing.syntax.traversing.nextDefinition

val asString: ValueTranslator = { it }
val asFloat: ValueTranslator = { it.toFloat() }
val asInt: ValueTranslator = { it.toInt() }

fun push(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  state.copy(
      burgStack = state.burgStack + BurgLayer(listOf(newBurg(burgType, valueTranslator)))
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
      burgStack = stack.dropLast(1) + BurgLayer(listOf(next))
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

//val popAppend: ParsingStateTransition = { _, state ->
//  val stack = state.burgStack
//  val shortStack = stack.dropLast(1)
//  val children = stack.last()
//  val newTop = shortStack.last()
//  state.copy(
//      burgStack = stack.dropLast(2)+BurgLayer(newTop + children)
//  )
//}

fun insertBelow(burgType: BurgType, valueTranslator: ValueTranslator): ParsingStateTransition = { newBurg, state ->
  val stack = state.burgStack
  state.copy(
      burgStack = stack
          .dropLast(1)
          + BurgLayer(listOf(newBurg(burgType, valueTranslator)))
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

fun foldToInclusive(burgType: BurgType): ParsingStateTransition = { newBurg, state ->
  if (state.burgStack.size < 2 || state.burgStack.last().burgs.first().type == burgType) {
    val stack = state.burgStack
    val head = stack.last().burgs.last()
    val newRange = head.range.copy(
        end = head.range.end.copy(
            index = head.range.end.index + newBurg(BurgType.block, asString).range.length
        )
    )
    val newHead = head.copy(range = newRange)
    val newTop = stack.last().burgs.dropLast(1).plus(newHead)
    state.copy(
        burgStack = stack.dropLast(1) + BurgLayer(newTop)
    )
  } else
    foldToInclusive(burgType)(newBurg, popChildren(state))
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
  if (state.contextStack.lastOrNull() == ContextMode.group)
    closeGroup(newBurg, state)
  else
    addError(TextId.missingOpeningParenthesis)(newBurg, state)
}

val tryCloseBlock: ParsingStateTransition = { newBurg, state ->
  if (state.contextStack.lastOrNull() == ContextMode.block)
    closeBlock(newBurg, state)
  else
    addError(TextId.invalidToken)(newBurg, state)
}

val checkGroupClosed: ParsingStateTransition = { newBurg, state ->
  if (state.contextStack.lastOrNull() == ContextMode.group)
    addError(TextId.missingClosingParenthesis)(newBurg, state)
  else
    skip(newBurg, state)
}

val tryNextDefinition: ParsingStateTransition = { newBurg, state ->
  if (state.contextStack.lastOrNull() == ContextMode.group)
    (addError(TextId.missingClosingParenthesis) + nextDefinition)(newBurg, state)
  else
    nextDefinition(newBurg, state)
}
