package silentorb.imp.core

data class Namespace(
    val connections: Set<Connection>,
    val references: Map<PathKey, PathKey>,
    val nodeTypes: Map<PathKey, TypeHash>,
    val values: Map<PathKey, Any>,
    val structures: Map<PathKey, Structure>,
    val typings: Typings,
    val numericTypeConstraints: Map<PathKey, NumericTypeConstraint>
) {
  val nodes: Set<PathKey>
    get() =
      connections
          .flatMap { listOf(it.source, it.destination) }
          .toSet()
          .plus(nodeTypes.keys)

  operator fun plus(other: Namespace): Namespace =
      mergeNamespaces(this, other)
}

typealias Graph = Namespace
