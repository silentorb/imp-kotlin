package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range

val emptyContext = listOf<Namespace>()

typealias NodeMap = Map<Id, Range>

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap
)

val emptyDungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(),
            connections = setOf(),
            functions = mapOf(),
            values = mapOf()
        ),
        nodeMap = mapOf()
    )
