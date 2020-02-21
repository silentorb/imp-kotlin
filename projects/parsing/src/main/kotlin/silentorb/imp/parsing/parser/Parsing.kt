package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  val tokens = tokenize(code)
  val errors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  if (errors.any())
    failure(errors)
  else
    parseTokens(context)(tokens)
}

fun parseTextOrThrow(context: Context, code: CodeBuffer, textLibrary: (TextId) -> String): Dungeon =
    parseText(context)(code)
        .throwOnFailure { Error(textLibrary(it.first().message)) }
