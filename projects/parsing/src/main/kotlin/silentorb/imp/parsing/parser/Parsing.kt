package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  tokenize(code)
      .then(parseTokens(context))
}
