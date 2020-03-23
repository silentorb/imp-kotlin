package silentorb.imp.execution

import silentorb.imp.core.*

typealias OutputValues = Map<Id, Any>
typealias Arguments = Map<String, Any>

fun nextStage(nodes: Set<Id>, connections: Collection<Connection>): List<Id> {
  return nodes.filter { node -> connections.none { it.destination == node } }
      .map { it }
}

fun arrangeGraphStages(graph: Graph): List<List<Id>> {
  var nodes = graph.nodes
  var connections = graph.connections
  var result = listOf<List<Id>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.source) }.toSet()
  }

  return result
}

fun arrangeGraphSequence(graph: Graph): List<Id> =
    arrangeGraphStages(graph).flatten()

fun prepareArguments(graph: Graph, outputValues: OutputValues, destination: Id): Arguments {
  return graph.connections
      .filter { it.destination == destination }
      .associate {
        val value = outputValues[it.source]!!
        Pair(it.parameter, value)
      }
}

fun executeNode(graph: Graph, functions: FunctionImplementationMap, values: OutputValues, id: Id): Any {
  return if (graph.values.containsKey(id)) {
    graph.values[id]!!
  } else {
    val type = graph.functionTypes[id]
    val signatureMatch = graph.signatureMatches[id]
    if (type != null && signatureMatch != null) {
      val function = functions[FunctionKey(type, signatureMatch.signature)]!!
      val arguments = prepareArguments(graph, values, id)
      function(arguments)
    } else {
      // Pass through
      val arguments = prepareArguments(graph, values, id)
      assert(arguments.size == 1)
      arguments.values.first()
    }
  }
}

fun executeStep(functions: FunctionImplementationMap, graph: Graph): (OutputValues, Id) -> OutputValues = { values, node ->
  values.plus(node to executeNode(graph, functions, values, node))
}

fun execute(functions: FunctionImplementationMap, graph: Graph, steps: List<Id>): OutputValues {
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

//fun getGraphOutput(graph: Graph, values: OutputValues): Map<Id, Any> {
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
