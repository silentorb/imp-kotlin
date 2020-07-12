package silentorb.imp.core

fun getGraphOutputNodes(namespace: Namespace): List<PathKey> =
    namespace.nodes.filter { node -> namespace.connections.none { it.value == node } }

fun getGraphOutputNode(options: List<PathKey>): PathKey? =
    if (options.size < 2)
      options.firstOrNull()
    else {
      val mainNode = options.firstOrNull { it.name == "main" }
      mainNode ?: options.last()
    }

fun getGraphOutputNode(namespace: Namespace): PathKey? {
  val nodes = getGraphOutputNodes(namespace)
  return getGraphOutputNode(nodes)
}

fun getGraphOutputNode(dungeon: Dungeon, filePath: String): PathKey? {
  val outputs = getGraphOutputNodes(dungeon.namespace)
  val fileOutputs = outputs
      .filter { node -> dungeon.nodeMap[node]?.file == filePath }

  return getGraphOutputNode(fileOutputs)
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

fun typesToTypeHash(types: Collection<TypeHash>): TypeHash? {
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
        output = completeSignature.output.hash,
        isVariadic = completeSignature.isVariadic
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
