package silentorb.imp.core

fun getGraphOutputNodes(graph: Graph): List<PathKey> =
    graph.connections
        .filter { connection -> graph.connections.none { it.source == connection.destination } }
        .map { it.destination }

fun getGraphOutputNode(graph: Graph): PathKey =
    getGraphOutputNodes(graph).first()

fun <K, V> associateWithNotNull(collection: Collection<K>, mapper: (K) -> V?): Map<K, V> =
    collection.mapNotNull {
      val value = mapper(it)
      if (value != null)
        Pair(it, value)
      else
        null
    }
        .associate { it }

fun signaturesToTypeHash(signatures: List<Signature>): TypeHash {
  assert(signatures.any())
  return if (signatures.size == 1)
    signatures.first().hashCode()
  else
    signatures.toSet().hashCode()
}
