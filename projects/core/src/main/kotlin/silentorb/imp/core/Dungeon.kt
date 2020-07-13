package silentorb.imp.core

typealias NodeMap = Map<PathKey, FileRange>

typealias ConstrainedLiteralMap = Map<PathKey, TypeHash>

data class Dungeon(
    val namespace: Namespace,
    val nodeMap: NodeMap
)

val emptyDungeon =
    Dungeon(
        namespace = newNamespace(),
        nodeMap = mapOf()
    )
