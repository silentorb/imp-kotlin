package silentorb.imp.core

typealias NamespaceMap = Map<Key, Namespace>

data class Namespace(
    val functions: FunctionMap = mapOf(),
    val namespaces: NamespaceMap = mapOf(),
    val nodes: Map<Key, Id> = mapOf(),
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

tailrec fun resolveNamespaceFunctionPath(namespace: Namespace, path: List<String>): Key? =
    if (path.none())
      null
    else {
      val name = path.first()
      val function = namespace.functions[name]
      if (function != null)
        function
      else {
        val child = namespace.namespaces[name]
        if (child == null)
          null
        else
          resolveNamespaceFunctionPath(child, path.drop(1))
      }
    }


tailrec fun resolveNamespacePath(namespace: Namespace, path: List<String>): Namespace? =
    if (path.none())
      namespace
    else {
      val name = path.first()
      val child = namespace.namespaces[name]
      if (child == null)
        null
      else
        resolveNamespacePath(child, path.drop(1))
    }
