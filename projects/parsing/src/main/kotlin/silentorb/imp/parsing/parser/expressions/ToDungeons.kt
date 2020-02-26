package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.Dungeon
import silentorb.imp.parsing.parser.ResolvedLiteral


fun literalToDungeon(id: Id, range: Range, literal: ResolvedLiteral): Dungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(
                id
            ),
            values = mapOf(
                id to literal.value
            ),
            types = mapOf(
                id to literal.type
            )
        ),
        nodeMap = mapOf(
            id to range
        )
    )

fun functionToDungeon(id: Id, range: Range, function: PathKey): Dungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(
                id
            ),
            types = mapOf(
                id to function
            )
        ),
        nodeMap = mapOf(
            id to range
        )
    )

fun nodeReferenceToDungeon(token: Token, nodeReference: NodeReference): Dungeon {
  val (id, type) = nodeReference
  return Dungeon(
      graph = Graph(
          nodes = setOf(id),
          types = mapOf(id to type)
      ),
      nodeMap = mapOf(
          id to token.range
      )
  )
}

fun nodeReferencesToDungeons(tokens: Tokens, nodeReferences: NodeReferenceMap): List<Dungeon> {
  return nodeReferences.map { (index, nodeReference) ->
    val (id, type) = nodeReference
    val token = tokens[index]
    nodeReferenceToDungeon(token, nodeReference)
  }
}

fun expressionToDungeons(nextId: NextId, tokens: Tokens, expressionResolution: ExpressionResolution): Map<Int, Dungeon> {
  val functions = expressionResolution.functions
      .mapValues { (tokenIndex, function) ->
        val token = tokens[tokenIndex]
        functionToDungeon(nextId(), token.range, function)
      }

  val nodeReferences = expressionResolution.nodeReferences
      .mapValues { (tokenIndex, nodeReference) ->
        val token = tokens[tokenIndex]
        nodeReferenceToDungeon(token, nodeReference)
      }

  val literals = expressionResolution.literals
      .mapValues { (tokenIndex, literal) ->
        val token = tokens[tokenIndex]
        literalToDungeon(nextId(), token.range, literal)
      }

  return functions.plus(nodeReferences).plus(literals)
}
