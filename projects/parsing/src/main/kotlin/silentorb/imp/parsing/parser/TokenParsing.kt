package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.parsing.structureOld.toTokenGraph
import silentorb.imp.parsing.structureOld.withoutComments
import java.nio.file.Paths

fun parseTokensToDungeon(context: Context, tokens: Tokens): ParsingResponse<Dungeon> {
  val filePath = Paths.get("")
  val (tokenGraph, tokenGraphErrors) = toTokenGraph(filePath.toString(), tokens)
  val importMap = mapOf(filePath to tokenGraph.imports)
  val definitions = tokenGraph.definitions
      .associateBy { definition ->
        PathKey("", definition.symbol.value as String)
      }
  val (dungeon, dungeonErrors) = parseDungeon(context, importMap, definitions)
  return ParsingResponse(
      dungeon,
      tokenGraphErrors + dungeonErrors
  )
}

fun parseTokensToDungeon(context: Context): (Tokens) -> ParsingResponse<Dungeon> = { tokens ->
  assert(context.any())
  if (tokens.none())
    ParsingResponse(emptyDungeon, listOf())
  else
    parseTokensToDungeon(context, withoutComments(tokens))
}

fun parseTextBranchingDeprecated(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  val tokens = stripWhitespace(tokenize(code))
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  if (lexingErrors.any())
    failure(lexingErrors)
  else {
    val (dungeon, parsingErrors) = parseTokensToDungeon(context)(tokens)
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

fun parseToDungeon(uri: TokenFile, context: Context, code: CodeBuffer): ParsingResponse<Dungeon> {
  val (tokens, lexingErrors) = tokenizeAndSanitize(uri, code)
  val (dungeon, parsingErrors) = parseTokensToDungeon(context)(tokens)
  return ParsingResponse(
      dungeon,
      lexingErrors.plus(parsingErrors)
  )
}

fun parseToDungeon(uri: TokenFile, context: Context): (CodeBuffer) -> ParsingResponse<Dungeon> = { code ->
  parseToDungeon(uri, context, code)
}
