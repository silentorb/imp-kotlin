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
          .plus(nodeTypes.filterValues { !typings.signatures.containsKey(it) }.keys)

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

typealias ContextIterator<K, V> = (Context, K) -> V?

tailrec fun <V> resolveContextField(context: Context, index: Int, getter: (Namespace) -> V?): V? =
    if (index < 0)
      null
    else
      getter(context[index])
          ?: resolveContextField(context, index - 1, getter)

fun <V> resolveContextField(getter: (Namespace) -> V?): (Context) -> V? = { context ->
  resolveContextField(context, context.size - 1, getter)
}

tailrec fun <V> resolveContextFieldGreedy(
    context: Context, index: Int, getter: (Namespace) -> List<V>,
    accumulator: List<V>
): List<V> =
    if (index < 0)
      accumulator
    else {
      val next = accumulator + getter(context[index])
      resolveContextFieldGreedy(context, index - 1, getter, next)
    }

fun <V> resolveContextFieldGreedy(context: Context, getter: (Namespace) -> List<V>): List<V> =
    resolveContextFieldGreedy(context, context.size - 1, getter, listOf())

tailrec fun <K, V> resolveContextFieldMap(
    context: Context, index: Int, getter: (Namespace) -> Map<K, V>,
    accumulator: Map<K, V>
): Map<K, V> =
    if (index < 0)
      accumulator
    else {
      val next = accumulator + getter(context[index])
      resolveContextFieldMap(context, index - 1, getter, next)
    }

fun <K, V> resolveContextFieldMap(context: Context, getter: (Namespace) -> Map<K, V>): Map<K, V> =
    resolveContextFieldMap(context, context.size - 1, getter, mapOf())

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

fun <V> resolveContextField(context: Context, getter: (Namespace) -> V?): V? =
    resolveContextField(context, context.size - 1, getter)

fun getNamespaceContents(context: Context, path: String): Map<PathKey, TypeHash> =
    resolveContextFieldMap(context) { namespace ->
      namespace.nodeTypes.filter { it.key.path == path }
    }

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
    resolveContextField(context) { namespace -> namespace.references[key] }

fun getTypeAlias(context: Context, key: TypeHash): TypeHash? =
    resolveContextField(context) { namespace -> namespace.typings.typeAliases[key] }

fun getTypeSignature(context: Context, key: TypeHash): Signature? =
    resolveContextField(context) { namespace -> namespace.typings.signatures[key] }

fun getTypeUnion(context: Context, key: TypeHash): Union? =
    resolveContextField(context) { namespace -> namespace.typings.unions[key] }

fun getSymbolType(context: Context, name: String): TypeHash? =
    typesToTypeHash(
        resolveContextFieldGreedy(context) { namespace ->
          namespace.nodeTypes.filterKeys { it.name == name }.values.toList()
        }
    )

fun getPathKeyTypes(context: Context, key: PathKey): List<TypeHash> {
  return resolveContextFieldGreedy(context) { namespace ->
    listOfNotNull(namespace.nodeTypes[key])
  }
}

fun flattenTypeSignatures(context: Context): (TypeHash) -> List<Signature> = { type ->
  resolveContextFieldGreedy(context) { namespace ->
    val signature = namespace.typings.signatures[type]
    if (signature != null)
      listOf(signature)
    else {
      val union = namespace.typings.unions[type]
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

fun resolveNumericTypeConstraint(key: TypeHash) =
    resolveContextField { namespace -> namespace.typings.numericTypeConstraints[key] }

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
          nodeTypes = namespace.nodeTypes + extractedTypings.entries.associate { Pair(it.value, it.key) },
          typings = namespace.typings.copy(
              typeNames = namespace.typings.typeNames + extractedTypings
          )
      )
}

fun getTypeNameOrNull(context: Context, type: TypeHash, step: Int = 0): PathKey? {
  return if (step > 50) {
    PathKey("", "infinite-recursion")
  } else {
    val directName = resolveContextField(context) { namespace ->
      namespace.typings.typeNames[type]
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
    getTypeNameOrNull(context, type, step) ?: unknownType.key
