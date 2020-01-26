package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune

fun parseExpression(nextId: NextId, context: Context, tokens: Tokens): Dungeon {
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

  val nodeMap = mapOf(
      id to token.range
  )

  val function = if (token.rune == Rune.identifier)
    resolveFunction(context, token.value)
  else
    null

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
  return dungeon
      .copy(
          graph = dungeon.graph.copy(
              nodes = nodes,
              functions = functions,
              values = values
          ),
          nodeMap = nodeMap
      )
}
