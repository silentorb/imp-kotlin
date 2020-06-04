package silentorb.imp.parsing.syntax

import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.core.newPosition
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun rootBurg(): PendingBurg =
    PendingBurg(
        type = BurgType.fileRoot,
        range = Range(
            start = newPosition(),
            end = newPosition()
        )
    )

fun finalizeBurg(file: TokenFile): (PendingBurg) -> Burg = { pendingBurg ->
  Burg(
      file = file,
      type = pendingBurg.type,
      range = pendingBurg.range,
      children = pendingBurg.children
  )
}

fun <T> stackAppend(stack: Stack<T>, item: T): Stack<T> =
    stack.dropLast(1).plusElement(stack.last() + item)
