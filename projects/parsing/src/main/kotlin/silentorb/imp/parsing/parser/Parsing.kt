package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  val tokens = tokenize(code)
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  if (lexingErrors.any())
    failure(lexingErrors)
  else {
    val (dungeon, parsingErrors) = parseTokens(context)(tokens)
    if (parsingErrors.any())
      failure(parsingErrors)
    else
      success(dungeon)
  }
}

fun parseTextOrThrow(context: Context, code: CodeBuffer, textLibrary: (TextId) -> String): Dungeon =
    parseText(context)(code)
        .throwOnFailure { Error(textLibrary(it.first().message)) }
