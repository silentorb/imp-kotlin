package silentorb.imp.core

fun getGraphOutputNodes(graph: Graph): List<PathKey> =
    graph.nodes.filter { node -> graph.connections.none { it.source == node } }

fun getGraphOutputNode(graph: Graph): PathKey? =
    getGraphOutputNodes(graph).firstOrNull()

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

fun typesToTypeHash(types: List<TypeHash>): TypeHash? {
  return if (types.none())
    null
  else if (types.size == 1)
    types.first()
  else
    types.toSet().hashCode()
}
