package silentorb.imp.core

typealias NodeMap = Map<PathKey, Range>

typealias ConstrainedLiteralMap = Map<PathKey, TypeHash>

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap,
    val implementationGraphs: Map<FunctionKey, Graph>
)

val emptyDungeon =
    Dungeon(
        graph = newNamespace(),
        nodeMap = mapOf(),
        implementationGraphs = mapOf()
    )
