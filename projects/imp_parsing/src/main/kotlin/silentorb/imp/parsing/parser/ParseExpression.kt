package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.NextId
import silentorb.imp.core.resolveFunction
import silentorb.imp.core.resolveNode
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseExpression(nextId: NextId, context: Context, tokens: Tokens): Response<Dungeon> {
  val token = tokens.first()
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

  return if (function == null && literalValue == null && referencedNode == null) {
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
//  val connections = if (referencedNode != null)
//    listOf(
//        Connection(
//            source = referencedNode,
//            destination =
//        )
//    )
//  else
//    listOf()

    val dungeon = emptyDungeon
    success(dungeon
        .copy(
            graph = dungeon.graph.copy(
                nodes = nodes,
                functions = functions,
                values = values
            ),
            nodeMap = nodeMap
        ))
  }
}
