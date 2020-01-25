package silentorb.imp.parsing.parser

import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.parsing.general.CodeBuffer
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.handle
import silentorb.imp.parsing.general.success

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  handle(tokenize(code)) { tokens ->
    success(emptyDungeon())
  }
}
