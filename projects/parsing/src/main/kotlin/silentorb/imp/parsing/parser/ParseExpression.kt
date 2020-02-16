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

fun parseArguments(nextId: NextId, context: Context, firstDungeon: Dungeon, tokens: GroupedTokens): Response<Dungeon> {
  return flatten(
      tokens
          .map(parseExpression(nextId, context))
  )
      .then { dungeons ->
        // Assuming that at least for now the first token only translates to a single node
        val destination = firstDungeon.graph.nodes.first()
        val callingSignature = dungeons.map { dungeon ->
          val output = getGraphOutputNode(dungeon.graph)
          dungeon.graph.types[output]
              ?: throw Error("Graph is missing a type for node $output")
        }
        val functionOverloads = getTypeDetails(context, firstDungeon.graph.types.values.first())!!
        matchFunction(callingSignature, functionOverloads, tokensToRange(tokens.map { getChildWithToken(it).token!! }))
            .map { (signature, parameterNames) ->
              dungeons.foldIndexed(firstDungeon) { i, a, b ->
                val source = getGraphOutputNode(b.graph)
                val parameter = parameterNames[i]
                mergeDistinctDungeons(a, b)
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

fun parseExpression(nextId: NextId, context: Context): (TokenGroup) -> Response<Dungeon> = { group ->
  val (token, children) = if (group.token != null)
    Pair(group.token, listOf())
  else
    Pair(getChildWithToken(group).token!!, group.children.drop(1))

  handle(parseExpressionToken(nextId, context)(token)) { firstDungeon ->
    if (group.children.size > 1) {
      parseArguments(nextId, context, firstDungeon, children)
    } else
      success(firstDungeon)
  }
}
