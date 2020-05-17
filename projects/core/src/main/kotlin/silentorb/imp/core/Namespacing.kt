package silentorb.imp.core

fun newTypings(): Typings =
    Typings(
        signatures = mapOf(),
        unions = mapOf()
    )

fun newNamespace(): Namespace =
    Namespace(
        connections = setOf(),
        nodeTypes = mapOf(),
        references = mapOf(),
        values = mapOf(),
        structures = mapOf(),
        typings = newTypings(),
        numericTypeConstraints = mapOf()
    )

fun mergeNamespaces(namespaces: Collection<Namespace>): Namespace =
    namespaces.reduce { accumulator, namespace ->
      Namespace(
          connections = accumulator.connections + namespace.connections,
          nodeTypes = accumulator.nodeTypes + namespace.nodeTypes,
          references = accumulator.references + namespace.references,
          structures = accumulator.structures + namespace.structures,
          numericTypeConstraints = accumulator.numericTypeConstraints + namespace.numericTypeConstraints,
          typings = mergeTypings(accumulator.typings, namespace.typings),
          values = accumulator.values + namespace.values
      )
    }

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


val resolveNumericTypeConstraint: ContextIterator<PathKey, NumericTypeConstraint> =
    resolveContextField { namespace, key -> namespace.numericTypeConstraints[key] }

fun namespaceFromOverloads(functions: OverloadsMap): Namespace {
  return newNamespace().copy(
      nodeTypes = functions.mapValues { signaturesToTypeHash(it.value) },
      typings = extractTypings(functions.values)
  )
}
