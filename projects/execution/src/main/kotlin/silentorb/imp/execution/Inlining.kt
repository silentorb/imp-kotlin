package silentorb.imp.execution

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.core.PathKey
import silentorb.imp.core.resolveReferenceValue

fun inlineValues(context: Context, graph: Graph, parameters: List<String>): Map<PathKey, Any> {
  val inputs = graph.connections
      .values
      .minus(graph.values.keys)
      .minus(graph.connections.keys.map { it.destination })
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
