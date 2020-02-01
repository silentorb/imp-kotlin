package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseExpressionToken(nextId: NextId, context: Context): (Token) -> Response<Dungeon> = { token ->
  val literalValue = parseTokenValue(token)
  val referencedNode = if (token.rune == Rune.identifier)
    resolveNode(context, token.value)
  else
    null

  val id = if (referencedNode != null)
    referencedNode
  else
    nextId()

  val nodes = setOf(id)
  val values = if (literalValue != null) {
    mapOf(
        id to literalValue
    )
  } else
    mapOf()

  val function = if (token.rune == Rune.identifier)
    resolveFunction(context, token.value)
  else
    null

  if (function == null && literalValue == null && referencedNode == null) {
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
            functions = functions,
            values = values
        ),
        nodeMap = nodeMap
    ))
  }
}

fun parseExpression(nextId: NextId, context: Context, tokens: Tokens): Response<Dungeon> {
  return handle(parseExpressionToken(nextId, context)(tokens.first())) { firstDungeon ->
    flatten(
        tokens
            .drop(1)
            .map(parseExpressionToken(nextId, context))
    )
        .map { dungeons ->
          // Assuming that at least for now the first token only translates to a single node
          val destination = firstDungeon.graph.nodes.first()
          val signature = getTypeDetails(context, firstDungeon.graph.functions.values.first())!!
          dungeons.foldIndexed(firstDungeon) { i, a, b ->
            val source = getGraphOutputNode(b.graph)
            val parameter = signature.parameterNames[i]
            addConnection(mergeDistinctDungeons(a, b), Connection(
                destination = destination,
                source = source,
                parameter = parameter
            ))
          }
        }
  }
}
