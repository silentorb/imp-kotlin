package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range

typealias NodeMap = Map<PathKey, Range>

typealias ConstrainedLiteralMap = Map<PathKey, PathKey>

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap,
    val literalConstraints: ConstrainedLiteralMap
) {
  fun addConnection(connection: Connection) =
      modifyGraph(this) { graph ->
        graph.copy(
            connections = graph.connections.plus(connection)
        )
      }

  fun addSignature(id: PathKey, signature: SignatureMatch) =
      modifyGraph(this) { graph ->
        graph.copy(
            signatureMatches = graph.signatureMatches.plus(Pair(id, signature))
        )
      }
}

val emptyDungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(),
            connections = setOf(),
            outputTypes = mapOf(),
            values = mapOf()
        ),
        nodeMap = mapOf(),
        literalConstraints = mapOf()
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
