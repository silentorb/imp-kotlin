package silentorb.imp.core

data class PathKey(
    val path: String,
    val name: String
)

data class Namespace(
    val functionAliases: Map<Key, PathKey> = mapOf(),
    val functions: OverloadsMap = mapOf(),
    val nodes: Map<Key, Id> = mapOf(),
    val types: Map<Id, PathKey> = mapOf(),
    val values: Map<Key, Any> = mapOf(),
    val structures: Map<PathKey, Structure> = mapOf(),
    val unions: Map<PathKey, List<Union>> = mapOf()
)

fun combineNamespaces(namespaces: Collection<Namespace>): Namespace =
    namespaces.reduce { accumulator, namespace ->
      accumulator.copy(
          functionAliases = accumulator.functionAliases.plus(namespace.functionAliases),
          functions = accumulator.functions.plus(namespace.functions),
          nodes = accumulator.nodes.plus(namespace.nodes),
          types = accumulator.types.plus(namespace.types),
          values = accumulator.values.plus(namespace.values),
          structures = accumulator.structures.plus(namespace.structures),
          unions = accumulator.unions.plus(namespace.unions)
      )
    }

fun combineNamespaces(vararg namespaces: Namespace): Namespace =
    combineNamespaces(namespaces.toList())

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
      context[index].functionAliases[name]
          ?: resolveFunction(context, name, index - 1)

fun resolveFunction(context: Context, name: String): PathKey? =
    resolveFunction(context, name, context.size - 1)

tailrec fun resolveNode(context: Context, name: String, index: Int): Pair<Id, PathKey>? =
    if (index < 0)
      null
    else {
      val id = context[index].nodes[name]

      if (id != null)
        Pair(id, context[index].types[id]!!)
      else
        resolveNode(context, name, index - 1)
    }

fun resolveNode(context: Context, name: String): Pair<Id, PathKey>? =
    resolveNode(context, name, context.size - 1)

tailrec fun getTypeDetails(context: Context, path: PathKey, index: Int): Signatures? =
    if (index < 0)
      null
    else
      context[index].functions[path]
          ?: getTypeDetails(context, path, index - 1)

fun getTypeDetails(context: Context, path: PathKey): Signatures? =
    getTypeDetails(context, path, context.size - 1)
