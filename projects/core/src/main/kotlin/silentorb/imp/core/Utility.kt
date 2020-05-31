package silentorb.imp.core

fun getGraphOutputNodes(graph: Graph): List<PathKey> =
    graph.nodes.filter { node -> graph.connections.none { it.value == node } }

fun getGraphOutputNode(graph: Graph): PathKey? {
  val nodes = getGraphOutputNodes(graph)
  return if (nodes.size < 2)
    nodes.firstOrNull()
  else {
    val mainNode = nodes.firstOrNull { it.name == "main" }
    return mainNode ?: nodes.last()
  }
}

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

fun convertCompleteSignature(completeSignature: CompleteSignature): Signature =
    Signature(
        parameters = completeSignature.parameters.map { parameter ->
          Parameter(
              parameter.name,
              parameter.type.hash
          )
        },
        output = completeSignature.output.hash
    )

fun joinPaths(vararg tokens: String): String =
    tokens
        .flatMap { it.split(".") }
        .filter { it.isNotEmpty() }
        .joinToString(".")

fun pathKeyToString(key: PathKey): String =
    joinPaths(key.path, key.name)

fun pathKeyFromString(value: String): PathKey {
  val tokens = value.split(".")
  return PathKey(tokens.dropLast(1).joinToString("."), tokens.lastOrNull() ?: "")
}
