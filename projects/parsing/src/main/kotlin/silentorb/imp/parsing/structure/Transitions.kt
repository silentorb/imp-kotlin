package silentorb.imp.parsing.structure

import silentorb.imp.core.FileRange
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.TextId

fun addChild(type: BurgType, mode: ParsingMode): ParsingTransition = { file, token, state ->
  val burg = newBurg(file, type, token)
  state.copy(
      burgs = state.burgs + (burg.hashCode() to burg),
      roads = state.roads + (burg.hashCode() to state.burgStack.last()),
      mode = mode
  )
}

fun addError(message: TextId): ParsingTransition = { file, token, state ->
  state.copy(
      errors = state.errors + ParsingError(
          message = message,
          fileRange = FileRange(file, token.range)
      )
  )
}

fun resetStack(mode: ParsingMode): ParsingTransition = { file, token, state ->
  state.copy(
      burgStack = state.burgStack.take(1),
      mode = mode
  )
}

val invalidToken = addError(TextId.invalidToken)
