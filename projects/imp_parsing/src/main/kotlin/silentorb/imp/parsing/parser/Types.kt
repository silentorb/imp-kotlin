package silentorb.imp.parsing.parser

import silentorb.imp.core.FunctionMap
import silentorb.imp.core.Graph
import silentorb.imp.core.Id
import silentorb.imp.parsing.general.Range

data class Context(
    val functions: FunctionMap,
    val namespaces: Map<Id, Context>,
    val values: Map<Id, Any>
)

fun emptyContext() = Context(
    functions = mapOf(),
    namespaces = mapOf(),
    values = mapOf()
)

typealias NodeMap = Map<Id, Range>
typealias ValueMap = NodeMap

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap,
    val valueMap: ValueMap
)

fun emptyDungeon() =
    Dungeon(
        graph = Graph(
            nodes = mapOf(),
            connections = setOf(),
            values = mapOf()
        ),
        nodeMap = mapOf(),
        valueMap = mapOf()
    )
