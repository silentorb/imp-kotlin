package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize
import java.net.URI
import java.nio.file.Paths

fun parseTokens(context: Context, tokens: Tokens): ParsingResponse<Dungeon> {
  val filePath = Paths.get("")
  val (tokenGraph, tokenGraphErrors) = toTokenGraph(filePath, tokens)
  val importMap = mapOf(filePath to tokenGraph.imports)
  val definitions = tokenGraph.definitions
      .associateBy { definition ->
        PathKey("", definition.symbol.value)
      }
  val (dungeon, dungeonErrors) = parseDungeon(context, importMap, definitions)
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

fun tokenizeAndSanitize(uri: TokenFile, code: CodeBuffer): ParsingResponse<Tokens> {
  val tokens = stripWhitespace(tokenize(code, uri))
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  return ParsingResponse(
      tokens,
      lexingErrors
  )
}

fun parseToDungeon(uri: TokenFile, context: Context): (CodeBuffer) -> ParsingResponse<Dungeon> = { code ->
  val (tokens, lexingErrors) = tokenizeAndSanitize(uri, code)
  val (dungeon, parsingErrors) = parseTokens(context)(tokens)
  ParsingResponse(
      dungeon,
      lexingErrors.plus(parsingErrors)
  )
}
