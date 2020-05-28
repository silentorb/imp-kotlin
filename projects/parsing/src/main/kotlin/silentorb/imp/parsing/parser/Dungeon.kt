package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Range

typealias NodeMap = Map<PathKey, Range>

typealias ConstrainedLiteralMap = Map<PathKey, TypeHash>

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap,
    val implementationGraphs: Map<FunctionKey, Graph>
)

val emptyDungeon =
    Dungeon(
        graph = newNamespace().copy(
            connections = mapOf(),
            values = mapOf()
        ),
        nodeMap = mapOf(),
        implementationGraphs = mapOf()
    )
