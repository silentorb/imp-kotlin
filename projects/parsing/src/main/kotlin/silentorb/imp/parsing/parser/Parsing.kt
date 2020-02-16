package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.parsing.general.CodeBuffer
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.lexer.tokenize

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  tokenize(code)
      .then(parseTokens(context))
}

fun parseTextOrThrow(context: Context, code: CodeBuffer, textLibrary: (TextId) -> String): Dungeon =
    parseText(context)(code)
        .throwOnFailure { Error(textLibrary(it.first().message)) }
