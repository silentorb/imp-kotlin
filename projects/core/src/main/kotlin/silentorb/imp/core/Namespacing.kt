package silentorb.imp.core

data class PathKey(
    val path: String,
    val name: String
)

typealias Aliases = Map<PathKey, PathKey>

data class Namespace(
    val references: Aliases,
    val localFunctionAliases: Map<Key, PathKey>,
    val functions: OverloadsMap,
    val nodes: Set<PathKey>,
    val types: Map<PathKey, PathKey>,
    val values: Map<Key, Any>,
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
        types = mapOf(),
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
          types = accumulator.types.plus(namespace.types),
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

tailrec fun resolveFunction(context: Context, name: String, index: Int): PathKey? =
    if (index < 0)
      null
    else
      context[index].localFunctionAliases[name]
          ?: resolveFunction(context, name, index - 1)

fun resolveFunction(context: Context, name: String): PathKey? =
    resolveFunction(context, name, context.size - 1)

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

tailrec fun getType(context: Context, key: PathKey, index: Int): PathKey? =
    if (index < 0)
      null
    else
      context[index].types[key] ?: getType(context, key, index - 1)

fun getType(context: Context, key: PathKey): PathKey? =
    getType(context, key, context.size - 1)

tailrec fun getTypeDetails(context: Context, path: PathKey, index: Int): Signatures? =
    if (index < 0)
      null
    else
      context[index].functions[path]
          ?: getTypeDetails(context, path, index - 1)

fun getTypeDetails(context: Context, path: PathKey): Signatures? =
    getTypeDetails(context, getRootType(context, path), context.size - 1)

tailrec fun resolveAlias(context: Context, key: PathKey, index: Int): PathKey =
    if (index < 0)
      key
    else
      context[index].references[key] ?: resolveAlias(context, key, index - 1)

fun resolveAlias(context: Context, key: PathKey): PathKey =
    resolveAlias(context, key, context.size - 1)

fun flattenAliases(context: Context): Aliases =
    context.fold(mapOf()) { a, b -> a.plus(b.references) }
