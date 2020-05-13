package silentorb.imp.execution

import silentorb.imp.core.*

typealias OutputValues = Map<PathKey, Any>
typealias Arguments = Map<String, Any>

fun nextStage(nodes: Set<PathKey>, connections: Collection<Connection>): List<PathKey> {
  return nodes.filter { node -> connections.none { it.destination == node } }
      .map { it }
}

fun arrangeGraphStages(graph: Graph): List<List<PathKey>> {
  var nodes = graph.nodes
  var connections = graph.connections
  var result = listOf<List<PathKey>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.source) }.toSet()
  }

  return result
}

fun arrangeGraphSequence(graph: Graph): List<PathKey> =
    arrangeGraphStages(graph).flatten()

fun prepareArguments(graph: Graph, outputValues: OutputValues, destination: PathKey): Arguments {
  return graph.connections
      .filter { it.destination == destination }
      .associate {
        val value = outputValues[it.source]!!
        Pair(it.parameter, value)
      }
}

fun executeNode(graph: Graph, functions: FunctionImplementationMap, values: OutputValues, id: PathKey,
                additionalArguments: Arguments? = null): Any {
  return if (graph.values.containsKey(id)) {
    graph.values[id]!!
  } else {
    val type = graph.references[id]
    val signatureMatch = graph.signatureMatches[id]
    if (type != null && signatureMatch != null) {
      val function = functions[FunctionKey(type, signatureMatch.signature)]!!
      val arguments = prepareArguments(graph, values, id)
      function(if (additionalArguments != null) arguments.plus(additionalArguments) else arguments)
    } else {
      // Pass through
      val arguments = prepareArguments(graph, values, id)
      assert(arguments.size == 1)
      arguments.values.first()
    }
  }
}

fun executeStep(functions: FunctionImplementationMap, graph: Graph): (OutputValues, PathKey) -> OutputValues = { values, node ->
  values.plus(node to executeNode(graph, functions, values, node))
}

fun executeStep(functions: FunctionImplementationMap, graph: Graph, values: OutputValues, node: PathKey, additionalArguments: Arguments) =
    values.plus(node to executeNode(graph, functions, values, node, additionalArguments))

fun execute(functions: FunctionImplementationMap, graph: Graph, steps: List<PathKey>): OutputValues {
  return steps.fold(mapOf(), executeStep(functions, graph))
}

fun execute(functions: FunctionImplementationMap, graph: Graph): OutputValues {
  val steps = arrangeGraphSequence(graph)
  return execute(functions, graph, steps)
}

fun executeToSingleValue(functions: FunctionImplementationMap, graph: Graph): Any {
  val result = execute(functions, graph)
  val output = getGraphOutputNode(graph)
  return result[output]!!
}

//fun getGraphOutput(graph: Graph, values: OutputValues): Map<PathKey, Any> {
//  val outputNode = getGraphOutputNode(graph)
//  return graph.connections
//      .filter { it.destination == outputNode }
//      .associate { Pair(it.parameter, values[it.source]!!) }
//}

//fun executeAndFormat(functions: FunctionImplementationMap, graph: Graph): Any {
//  val values = execute(functions, graph)
//
//  return getGraphOutput(graph, values)
//}
