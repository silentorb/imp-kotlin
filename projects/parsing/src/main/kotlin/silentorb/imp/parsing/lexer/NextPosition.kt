package silentorb.imp.parsing.lexer

import silentorb.imp.parsing.general.Position

fun nextPosition(character: Char?, position: Position): Position =
    when (character) {
      null -> position
      '\n' -> position.copy(index = position.index + 1, column = 1, row = position.row + 1)
      else -> position.copy(index = position.index + 1, column = position.column + 1)
    }
