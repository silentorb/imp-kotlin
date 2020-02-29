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
    val signature = graph.signatures[id]
    if (type != null && signature != null) {
      val function = functions[FunctionKey(type, signature)]!!
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

fun executeStage(graph: Graph, functions: FunctionImplementationMap): (OutputValues, List<Id>) -> OutputValues = { values, stage ->
  val newValues = stage.map { id ->
    Pair(id, executeNode(graph, functions, values, id))
  }
      .associate { it }
  values.plus(newValues)
}

fun execute(functions: FunctionImplementationMap, graph: Graph, stages: List<List<Id>>): OutputValues {
  return stages.fold(mapOf(), executeStage(graph, functions))
}

fun execute(functions: FunctionImplementationMap, graph: Graph): OutputValues {
  val stages = arrangeGraphStages(graph)
  return execute(functions, graph, stages)
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
