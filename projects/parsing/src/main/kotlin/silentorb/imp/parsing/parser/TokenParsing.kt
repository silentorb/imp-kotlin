package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.parsing.resolution.parseDungeon
import silentorb.imp.parsing.syntax.toTokenGraph
import silentorb.imp.parsing.syntax.withoutComments
import java.nio.file.Path
import java.nio.file.Paths

fun parseTokensToDungeon(filePath: TokenFile, context: Context, tokens: Tokens): Response<Dungeon> {
  val (tokenGraph, tokenGraphErrors) = toTokenGraph(filePath, tokens)
  val importMap = mapOf(Paths.get(filePath) to tokenGraph.imports)
  val definitions = tokenGraph.definitions
      .associateBy { definition ->
        PathKey("", definition.symbol.value as String)
      }
  val (dungeon, dungeonErrors) = parseDungeon(context, importMap, definitions)
  return Response(
      dungeon,
      tokenGraphErrors + dungeonErrors
  )
}

fun parseTokensToDungeon(filePath: TokenFile, context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  assert(context.any())
  if (tokens.none())
    Response(emptyDungeon, listOf())
  else
    parseTokensToDungeon(filePath, context, withoutComments(tokens))
}

fun tokenizeAndSanitize(uri: TokenFile, code: CodeBuffer): Response<Tokens> {
  val tokens = stripWhitespace(tokenize(code, uri))
  val lexingErrors = tokens.filter { it.rune == Rune.bad }
      .map { newParsingError(TextId.unexpectedCharacter, it) }

  return Response(
      tokens,
      lexingErrors
  )
}

fun parseToDungeon(uri: TokenFile, context: Context, code: CodeBuffer): Response<Dungeon> {
  val (tokens, lexingErrors) = tokenizeAndSanitize(uri, code)
  val (dungeon, parsingErrors) = parseTokensToDungeon(uri, context)(tokens)
  return Response(
      dungeon,
      lexingErrors.plus(parsingErrors)
  )
}
fun parseToDungeon(context: Context, code: CodeBuffer): Response<Dungeon> =
    parseToDungeon("", context, code)

fun parseToDungeon(uri: TokenFile, context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  parseToDungeon(uri, context, code)
}
