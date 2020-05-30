package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Dungeon
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize
import java.net.URI

fun parseTextBranchingDeprecated(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  val tokens = stripWhitespace(tokenize(code))
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

fun parseText(uri: URI, context: Context): (CodeBuffer) -> ParsingResponse<Dungeon> = { code ->
  val tokens = stripWhitespace(tokenize(code, uri))
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  val (dungeon, parsingErrors) = parseTokens(context)(tokens)
  ParsingResponse(
      dungeon,
      lexingErrors.plus(parsingErrors)
  )
}
