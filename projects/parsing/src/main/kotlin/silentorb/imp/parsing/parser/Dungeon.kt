package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range

typealias NodeMap = Map<PathKey, Range>

typealias ConstrainedLiteralMap = Map<PathKey, TypeHash>

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

//  fun addSignature(id: PathKey, signature: SignatureMatch) =
//      modifyGraph(this) { graph ->
//        graph.copy(
//            signatureMatches = graph.signatureMatches.plus(Pair(id, signature))
//        )
//      }
}

val emptyDungeon =
    Dungeon(
        graph = newNamespace().copy(
//            nodes = setOf(),
            connections = setOf(),
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
