package silentorb.imp.execution

import silentorb.imp.core.Graph
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultParameter

fun prepareArguments(graph: Graph, outputValues: OutputValues, destination: PathKey): Arguments {
  return graph.connections
      .filter { it.key.destination == destination && it.key.parameter != defaultParameter }
      .entries
      .associate {
        val value = outputValues[it.value]!!
        Pair(it.key.parameter, value)
      }
}
