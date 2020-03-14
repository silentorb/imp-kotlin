package silentorb.imp.core

data class PathKey(
    val path: String,
    val name: String
)

typealias Aliases = Map<PathKey, PathKey>

data class Namespace(
    val aliases: Aliases,
    val localFunctionAliases: Map<Key, PathKey>,
    val functions: OverloadsMap,
    val nodes: Map<Key, Id>,
    val types: Map<Id, PathKey>,
    val values: Map<Key, Any>,
    val structures: Map<PathKey, Structure>,
    val unions: Map<PathKey, List<Union>>,
    val numericTypeConstraints: Map<PathKey, NumericTypeConstraint>
)

fun newNamespace(): Namespace =
    Namespace(
        aliases = mapOf(),
        localFunctionAliases = mapOf(),
        functions = mapOf(),
        nodes = mapOf(),
        types = mapOf(),
        values = mapOf(),
        structures = mapOf(),
        unions = mapOf(),
        numericTypeConstraints = mapOf()
    )

fun mergeNamespaces(namespaces: Collection<Namespace>): Namespace =
    namespaces.reduce { accumulator, namespace ->
      Namespace(
          aliases = accumulator.aliases.plus(namespace.aliases),
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

typealias NodeReference = Pair<Id, PathKey>

tailrec fun resolveNode(context: Context, name: String, index: Int): NodeReference? =
    if (index < 0)
      null
    else {
      val id = context[index].nodes[name]

      if (id != null) {
        val type = context[index].types[id]
        if (type != null)
          Pair(id, type)
        else
          null
      } else
        resolveNode(context, name, index - 1)
    }

fun resolveNode(context: Context, name: String): NodeReference? =
    resolveNode(context, name, context.size - 1)

tailrec fun getTypeDetails(context: Context, path: PathKey, index: Int): Signatures? =
    if (index < 0)
      null
    else
      context[index].functions[path]
          ?: getTypeDetails(context, path, index - 1)

fun getTypeDetails(context: Context, path: PathKey): Signatures? =
    getTypeDetails(context, path, context.size - 1)

fun flattenAliases(context: Context): Aliases =
    context.fold(mapOf()) { a, b -> a.plus(b.aliases) }
