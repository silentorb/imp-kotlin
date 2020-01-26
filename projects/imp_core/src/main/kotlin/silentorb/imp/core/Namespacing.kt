package silentorb.imp.core

data class Namespace(
    val functions: FunctionMap = mapOf(),
    val namespaces: Map<Key, Namespace> = mapOf(),
    val nodes: Map<Key, Id>,
    val values: Map<Key, Any> = mapOf()
)

typealias Context = List<Namespace>

tailrec fun resolveFunction(context: Context, name: String, index: Int): Path? =
    if (index < 0)
      null
    else
      context[index].functions[name]
          ?: resolveFunction(context, name, index - 1)

fun resolveFunction(context: Context, name: String): Path? =
    resolveFunction(context, name, context.size - 1)

tailrec fun resolveNode(context: Context, name: String, index: Int): Id? =
    if (index < 0)
      null
    else
      context[index].nodes[name]
          ?: resolveNode(context, name, index - 1)

fun resolveNode(context: Context, name: String): Id? =
    resolveNode(context, name, context.size - 1)
