package silentorb.imp.core

data class PathKey(
    val path: String,
    val name: String
)

data class Namespace(
    val functionAliases: Map<Key, PathKey> = mapOf(),
    val functions: OverloadsMap = mapOf(),
    val nodes: Map<Key, Id> = mapOf(),
    val values: Map<Key, Any> = mapOf()
)

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

tailrec fun resolveNode(context: Context, name: String, index: Int): Id? =
    if (index < 0)
      null
    else
      context[index].nodes[name]
          ?: resolveNode(context, name, index - 1)

fun resolveNode(context: Context, name: String): Id? =
    resolveNode(context, name, context.size - 1)

tailrec fun getTypeDetails(context: Context, path: PathKey, index: Int): Overloads? =
    if (index < 0)
      null
    else
      context[index].functions[path]
          ?: getTypeDetails(context, path, index - 1)

fun getTypeDetails(context: Context, path: PathKey): Overloads? =
    getTypeDetails(context, path, context.size - 1)
