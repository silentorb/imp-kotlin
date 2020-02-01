package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range

typealias NodeMap = Map<Id, Range>

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap
) {
  fun addConnection(connection: Connection) =
      modifyGraph(this) { graph ->
        graph.copy(
            connections = graph.connections.plus(connection)
        )
      }

  fun addSignature(id: Id, signature: Signature) =
      modifyGraph(this) { graph ->
        graph.copy(
            signatures = graph.signatures.plus(Pair(id, signature))
        )
      }

}

val emptyDungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(),
            connections = setOf(),
            types = mapOf(),
            values = mapOf()
        ),
        nodeMap = mapOf()
    )

fun modifyGraph(dungeon: Dungeon, transform: (Graph) -> Graph) =
    dungeon.copy(
        graph = transform(dungeon.graph)
    )

fun addConnection(dungeon: Dungeon, connection: Connection) =
    modifyGraph(dungeon) { graph ->
      graph.copy(
          connections = graph.connections.plus(connection)
      )
    }
