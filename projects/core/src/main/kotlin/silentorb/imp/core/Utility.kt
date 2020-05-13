package silentorb.imp.core

fun getGraphOutputNodes(graph: Graph): List<PathKey> =
    graph.nodes.filter { node -> graph.connections.none { it.source == node } }

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
