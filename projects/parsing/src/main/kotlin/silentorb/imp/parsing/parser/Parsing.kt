package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Dungeon
import silentorb.imp.core.PathKey
import silentorb.imp.core.emptyDungeon
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize
import java.net.URI

fun parseTokens(context: Context, tokens: Tokens): ParsingResponse<Dungeon> {
  val (tokenGraph, tokenGraphErrors) = toTokenGraph(tokens)
  val (dungeon, dungeonErrors) = parseDungeon(context, mapOf(PathKey("", "") to tokenGraph))
  return ParsingResponse(
      dungeon,
      tokenGraphErrors + dungeonErrors
  )
}

fun parseTokens(context: Context): (Tokens) -> ParsingResponse<Dungeon> = { tokens ->
  assert(context.any())
  if (tokens.none())
    ParsingResponse(emptyDungeon, listOf())
  else
    parseTokens(context, withoutComments(tokens))
}

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

fun tokenizeAndSanitize(uri: URI, code: CodeBuffer): ParsingResponse<Tokens> {
  val tokens = stripWhitespace(tokenize(code, uri))
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  return ParsingResponse(
      tokens,
      lexingErrors
  )
}

fun parseToDungeon(uri: URI, context: Context): (CodeBuffer) -> ParsingResponse<Dungeon> = { code ->
  val (tokens, lexingErrors) = tokenizeAndSanitize(uri, code)
  val (dungeon, parsingErrors) = parseTokens(context)(tokens)
  ParsingResponse(
      dungeon,
      lexingErrors.plus(parsingErrors)
  )
}
