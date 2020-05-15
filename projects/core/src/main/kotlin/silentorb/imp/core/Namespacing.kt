package silentorb.imp.core

data class PathKey(
    val path: String,
    val name: String
)

typealias Aliases = Map<PathKey, PathKey>

data class Namespace(
    val references: Map<PathKey, PathKey>,
    val localFunctionAliases: Map<Key, PathKey>,
    val functions: OverloadsMap,
    val nodes: Set<PathKey>,
    val values: Map<PathKey, Any>,
    val structures: Map<PathKey, Structure>,
    val unions: Map<PathKey, List<Union>>,
    val numericTypeConstraints: Map<PathKey, NumericTypeConstraint>
)

fun newNamespace(): Namespace =
    Namespace(
        references = mapOf(),
        localFunctionAliases = mapOf(),
        functions = mapOf(),
        nodes = setOf(),
        values = mapOf(),
        structures = mapOf(),
        unions = mapOf(),
        numericTypeConstraints = mapOf()
    )

fun mergeNamespaces(namespaces: Collection<Namespace>): Namespace =
    namespaces.reduce { accumulator, namespace ->
      Namespace(
          references = accumulator.references.plus(namespace.references),
          localFunctionAliases = accumulator.localFunctionAliases.plus(namespace.localFunctionAliases),
          functions = accumulator.functions.plus(namespace.functions),
          nodes = accumulator.nodes.plus(namespace.nodes),
          values = accumulator.values.plus(namespace.values),
          structures = accumulator.structures.plus(namespace.structures),
          unions = accumulator.unions.plus(namespace.unions),
          numericTypeConstraints = accumulator.numericTypeConstraints.plus(namespace.numericTypeConstraints)
      )
    }

fun mergeNamespaces(vararg namespaces: Namespace): Namespace =
    mergeNamespaces(namespaces.toList())

typealias Context = List<Namespace>

fun toPathString(list: List<String>) =
    list.joinToString(".")

fun toPathKey(list: List<String>) =
    PathKey(toPathString(list.dropLast(1)), list.takeLast(1).first())

fun getDirectoryContents(namespace: Namespace, path: String): Set<PathKey> =
    namespace.functions
        .filterKeys { it.path == path }
        .keys

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

fun <K, V> resolveContextField(context: Context, key: K, getter: (Namespace, K) -> V?): V? =
    resolveContextField(context, key, context.size - 1, getter)

val resolveFunction: ContextIterator<Key, PathKey> =
    resolveContextField { namespace, key -> namespace.localFunctionAliases[key] }

tailrec fun resolveReference(context: Context, name: String, index: Int): PathKey? =
    if (index < 0)
      null
    else {
      val nodes = context[index].nodes.filter { it.name == name }
          .plus(context[index].functions.keys.filter { it.name == name })

      if (nodes.size > 1)
        throw Error("Not yet supported")

      nodes.firstOrNull() ?: resolveReference(context, name, index - 1)
    }

fun resolveReference(context: Context, name: String): PathKey? =
    resolveReference(context, name, context.size - 1)

fun getTypeDetails(context: Context, path: PathKey): Signatures? =
    resolveContextField(context, getRootType(context, path)) { namespace, key -> namespace.functions[key] }

fun resolveAlias(context: Context, key: PathKey): PathKey =
    resolveContextField(context, key) { namespace, k -> namespace.references[k] } ?: key

val resolveNumericTypeConstraint: ContextIterator<PathKey, NumericTypeConstraint> =
    resolveContextField { namespace, key -> namespace.numericTypeConstraints[key] }
