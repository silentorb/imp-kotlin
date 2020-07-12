package silentorb.imp.execution

import silentorb.imp.core.Context
import silentorb.imp.core.Namespace
import silentorb.imp.core.PathKey
import silentorb.imp.core.resolveReferenceValue

fun inlineValues(context: Context, namespace: Namespace, parameters: List<String>): Map<PathKey, Any> {
  val inputs = namespace.connections
      .values
      .minus(namespace.values.keys)
      .minus(namespace.connections.keys.map { it.destination })
      .filter { !parameters.contains(it.name) }
      .distinct()

  return inputs
      .mapNotNull { input ->
        val value = resolveReferenceValue(context, input)
        if (value != null)
          Pair(input, value)
        else
          null
      }
      .associate { it }
}
