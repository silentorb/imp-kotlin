package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.core.Id
import silentorb.imp.core.Key
import silentorb.imp.parsing.general.Range

val emptyContext = Context(
    functions = mapOf(),
    namespaces = mapOf(),
    values = mapOf()
)

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
