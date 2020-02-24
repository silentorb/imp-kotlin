package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseExpressionToken(nextId: NextId, context: Context): (Token) -> Response<Dungeon> = { token ->
  val literalValuePair = parseTokenValue(token)
  val referencedNode = if (token.rune == Rune.identifier)
    resolveNode(context, token.value)
  else
    null

  if (referencedNode != null) {
    val (id, type) = referencedNode
    success(Dungeon(
        graph = Graph(
            nodes = setOf(id),
            types = mapOf(id to type)
        ),
        nodeMap = mapOf(
            id to token.range
        )
    ))
  } else {
    val id = nextId()

    val nodes = setOf(id)
    val values = if (literalValuePair != null) {
      mapOf(
          id to literalValuePair.second
      )
    } else
      mapOf()

    val function = if (literalValuePair != null)
      literalValuePair.first
    else if (token.rune == Rune.identifier || token.rune == Rune.operator)
      resolveFunction(context, token.value)
    else
      null

    if (function == null && literalValuePair == null) {
      failure(newParsingError(TextId.unknownFunction, token))
    } else {
      val nodeMap = mapOf(
          id to token.range
      )
      val functions = if (function != null)
        mapOf(
            id to function
        )
      else
        mapOf()

      success(Dungeon(
          graph = Graph(
              nodes = nodes,
              types = functions,
              values = values
          ),
          nodeMap = nodeMap
      ))
    }
  }
}

fun parseNamedArgument(nextId: NextId, context: Context, tokens: GroupedTokens): Response<Pair<Token?, Dungeon>> {
  return if (tokens.size == 1) {
    failure(newParsingError(TextId.missingArgumentName, tokens[0].token!!))
  } else if (tokens.size == 2) {
    failure(newParsingError(TextId.missingArgumentExpression, tokens[1].token!!))
  } else {
    val symbolToken = getChildWithToken(tokens[1]).token!!
    if (symbolToken.rune != Rune.identifier)
      failure(newParsingError(TextId.expectedIdentifier, symbolToken))
    else {
      val first = parseExpression(nextId, context)(tokens[2])
          .map { Pair<Token?, Dungeon>(symbolToken, it) }
      if (tokens.size > 3) {
        flatten(listOf(
            first,
            parseNamedArgumentOrExpression(nextId, context, tokens.drop(3))
        ))
      }
      else
        first
    }
  }
}

fun parseNamedArgumentOrExpression(nextId: NextId, context: Context, tokens: GroupedTokens): Response<List<Pair<Token?, Dungeon>>> {
  val token = getChildWithToken(tokens.first()).token!!
  return if (token.rune == Rune.operator && token.value == "<=")
    parseNamedArgument(nextId, context, tokens)
        .map { listOf(it) }
  else
    flatten(tokens.map {
      parseExpression(nextId, context)(it)
          .map { Pair<Token?, Dungeon>(null, it) }
    })
}

fun parseArguments(nextId: NextId, context: Context, firstDungeon: Dungeon, tokens: GroupedTokens): Response<Dungeon> {
  return parseNamedArgumentOrExpression(nextId, context, tokens)
      .then { dungeonPairs ->
        // Assuming that at least for now the first token only translates to a single node
        val destination = firstDungeon.graph.nodes.first()
        val arguments = dungeonPairs.map { (symbol, dungeon) ->
          val output = getGraphOutputNode(dungeon.graph)
          val type = dungeon.graph.types[output]
              ?: throw Error("Graph is missing a type for node $output")
          Argument(
              name = symbol?.value,
              type = type
          )
        }
        val functionOverloads = getTypeDetails(context, firstDungeon.graph.types.values.first())!!
        matchFunction(arguments, functionOverloads, tokensToRange(tokens.map { getChildWithToken(it).token!! }))
            .map { signature ->
              dungeonPairs.foldIndexed(firstDungeon) { i, a, (symbol, dungeon) ->
                val source = getGraphOutputNode(dungeon.graph)
                val parameter = symbol?.value ?: signature.parameters[i].name
                mergeDistinctDungeons(a, dungeon)
                    .addConnection(Connection(
                        destination = destination,
                        source = source,
                        parameter = parameter
                    ))
              }
                  .addSignature(destination, signature)
            }
      }
}

fun parseExpression(nextId: NextId, context: Context): (ExpressionGraph) -> Dungeon = { graph ->

//  val (token, children) = if (group.token != null)
//    Pair(group.token, listOf())
//  else
//    Pair(getChildWithToken(group).token!!, group.children.drop(1))
//
//  handle(parseExpressionToken(nextId, context)(token)) { firstDungeon ->
//    if (group.children.size > 1) {
//      parseArguments(nextId, context, firstDungeon, children)
//    } else
//      success(firstDungeon)
//  }
}
