package silentorb.imp.core

data class Namespace(
    val connections: Connections,
    val nodeTypes: Map<PathKey, TypeHash>,
    val values: Map<PathKey, Any>,
    val typings: Typings
) {
  val nodes: Set<PathKey>
    get() =
      nodeTypes.keys

  operator fun plus(other: Namespace): Namespace =
      mergeNamespaces(this, other)
}

fun newNamespace(): Namespace =
    Namespace(
        connections = mapOf(),
        nodeTypes = mapOf(),
        values = mapOf(),
        typings = newTypings()
    )

fun mergeNamespaces(first: Namespace, second: Namespace): Namespace =
    Namespace(
        connections = first.connections + second.connections,
        nodeTypes = first.nodeTypes + second.nodeTypes,
        values = first.values + second.values,
        typings = mergeTypings(first.typings, second.typings)
    )

fun mergeNamespaces(namespaces: Collection<Namespace>): Namespace =
    namespaces.reduce(::mergeNamespaces)

fun mergeNamespaces(vararg namespaces: Namespace): Namespace =
    mergeNamespaces(namespaces.toList())

typealias Context = List<Namespace>

fun toPathString(list: List<String>) =
    list.joinToString(".")

fun toPathKey(list: List<String>) =
    PathKey(toPathString(list.dropLast(1)), list.takeLast(1).first())
