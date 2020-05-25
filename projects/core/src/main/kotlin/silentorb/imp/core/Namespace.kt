package silentorb.imp.core

data class Namespace(
    val connections: Set<Connection>,
    val references: Map<PathKey, PathKey>,
    val implementationTypes: Map<PathKey, TypeHash>,
    val nodeTypes: Map<PathKey, TypeHash>,
    val values: Map<PathKey, Any>,
    val typings: Typings
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

fun newNamespace(): Namespace =
    Namespace(
        connections = setOf(),
        implementationTypes = mapOf(),
        nodeTypes = mapOf(),
        references = mapOf(),
        values = mapOf(),
        typings = newTypings()
    )

fun mergeNamespaces(first: Namespace, second: Namespace): Namespace =
    Namespace(
        connections = first.connections + second.connections,
        implementationTypes = first.implementationTypes + second.implementationTypes,
        nodeTypes = first.nodeTypes + second.nodeTypes,
        references = first.references + second.references,
        typings = mergeTypings(first.typings, second.typings),
        values = first.values + second.values
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

fun getDirectoryContents(namespace: Namespace, path: String): Map<PathKey, TypeHash> =
    namespace.nodeTypes
        .filter { it.key.path == path }

typealias ContextIterator<K, V> = (Context, K) -> V?

tailrec fun <K, V> resolveContextField(context: Context, key: K, index: Int, getter: (Namespace, K) -> V?): V? =
    if (index < 0)
      null
    else
      getter(context[index], key)
          ?: resolveContextField(context, key, index - 1, getter)

fun <K, V> resolveContextField(getter: (Namespace, K) -> V?): (Context, K) -> V? = { context, key ->
  resolveContextField(context, key, context.size - 1, getter)
}

tailrec fun <K, V> resolveContextFieldGreedy(
    context: Context, key: K, index: Int, getter: (Namespace, K) -> List<V>,
    accumulator: List<V>
): List<V> =
    if (index < 0)
      accumulator
    else {
      val next = accumulator + getter(context[index], key)
      resolveContextFieldGreedy(context, key, index - 1, getter, next)
    }

fun <K, V> resolveContextFieldGreedy(context: Context, key: K, getter: (Namespace, K) -> List<V>): List<V> =
    resolveContextFieldGreedy(context, key, context.size - 1, getter, listOf())

tailrec fun <K, V> resolveContextFieldGreedySet(
    context: Context, key: K, index: Int, getter: (Namespace, K) -> Set<V>,
    accumulator: Set<V>
): Set<V> =
    if (index < 0)
      accumulator
    else {
      val next = accumulator + getter(context[index], key)
      resolveContextFieldGreedySet(context, key, index - 1, getter, next)
    }

fun <K, V> resolveContextFieldGreedySet(context: Context, key: K, getter: (Namespace, K) -> Set<V>): Set<V> =
    resolveContextFieldGreedySet(context, key, context.size - 1, getter, setOf())

fun <K, V> resolveContextField(context: Context, key: K, getter: (Namespace, K) -> V?): V? =
    resolveContextField(context, key, context.size - 1, getter)

tailrec fun resolveReference(context: Context, name: String, index: Int): PathKey? =
    if (index < 0)
      null
    else {
      val nodes = context[index].nodeTypes.keys.filter { it.name == name }
          .plus(context[index].references.keys.filter { it.name == name })

      if (nodes.size > 1)
        throw Error("Not yet supported")

      nodes.firstOrNull() ?: resolveReference(context, name, index - 1)
    }

fun resolveReference(context: Context, name: String): PathKey? =
    resolveReference(context, name, context.size - 1)

fun resolveAlias(context: Context, key: PathKey): PathKey? =
    resolveContextField(context, key) { namespace, k -> namespace.references[k] }

fun getTypeAlias(context: Context, key: TypeHash): TypeHash? =
    resolveContextField(context, key) { namespace, k -> namespace.typings.typeAliases[k] }

fun getTypeSignature(context: Context, key: TypeHash): Signature? =
    resolveContextField(context, key) { namespace, k -> namespace.typings.signatures[k] }

fun getTypeUnion(context: Context, key: TypeHash): Union? =
    resolveContextField(context, key) { namespace, k -> namespace.typings.unions[k] }

fun getSymbolType(context: Context, name: String): TypeHash? =
    typesToTypeHash(
        resolveContextFieldGreedy(context, name) { namespace, key ->
          namespace.nodeTypes.filterKeys { it.name == name }.values.toList()
        }
    )

fun getPathKeyTypes(context: Context, pathKey: PathKey): List<TypeHash> =
    resolveContextFieldGreedy(context, resolveAlias(context, pathKey)) { namespace, key ->
      listOfNotNull(namespace.nodeTypes[key])
    }

fun flattenTypeSignatures(context: Context): (TypeHash) -> List<Signature> = { type ->
  resolveContextFieldGreedy(context, type) { namespace, key ->
    val signature = namespace.typings.signatures[key]
    if (signature != null)
      listOf(signature)
    else {
      val union = namespace.typings.unions[key]
      union?.flatMap(flattenTypeSignatures(context)) ?: listOf()
    }
  }
}

fun getTypeSignatures(context: Context, pathKey: PathKey): List<Signature> {
  val types = getPathKeyTypes(context, pathKey)
  return types
      .flatMap(flattenTypeSignatures(context))
      .distinct()
}

val resolveNumericTypeConstraint: ContextIterator<TypeHash, NumericTypeConstraint> =
    resolveContextField { namespace, key -> namespace.typings.numericTypeConstraints[key] }

fun namespaceFromOverloads(functions: OverloadsMap): Namespace {
  return newNamespace().copy(
      nodeTypes = functions.mapValues { signaturesToTypeHash(it.value) },
      typings = extractTypings(functions.values)
  )
}

fun namespaceFromCompleteOverloads(signatures: Map<PathKey, List<CompleteSignature>>): Namespace {
  val namespace = namespaceFromOverloads(signatures.mapValues { it.value.map(::convertCompleteSignature) })
  val extractedTypings = signatures.values.flatten()
      .fold(mapOf<TypeHash, PathKey>()) { a, signature ->
        a + signature.parameters
            .associate { Pair(it.type.hash, it.type.key) }
            .plus(signature.output.hash to signature.output.key)
      }
  return namespace
      .copy(
          typings = namespace.typings.copy(
              typeNames = namespace.typings.typeNames + extractedTypings
          )
      )
}

fun getTypeNameOrNull(context: Context, type: TypeHash, step: Int = 0): PathKey? {
  return if (step > 50) {
    PathKey("", "infinite-recursion")
  } else {
    val directName = resolveContextField(context, type) { namespace, key ->
      namespace.typings.typeNames[key]
    }
    if (directName != null)
      directName
    else {
      val signature = getTypeSignature(context, type)
      if (signature != null) {
        PathKey("",
            signature.parameters.map { parameter ->
              "${parameter.name}: ${getTypeNameOrUnknown(context, parameter.type, step + 1)}"
            }
                .plus(listOf(getTypeNameOrUnknown(context, signature.output, step + 1)))
                .joinToString(" -> ")
        )
      } else {
        val union = getTypeUnion(context, type)
        if (union != null) {
          PathKey("",
              union
                  .map { option -> getTypeNameOrUnknown(context, option, step + 1) }
                  .joinToString(" -> ")
          )
        } else
          null
      }
    }
  }
}

fun getTypeNameOrUnknown(context: Context, type: TypeHash, step: Int = 0): PathKey =
    getTypeNameOrNull(context, type, step) ?: unknownKey
