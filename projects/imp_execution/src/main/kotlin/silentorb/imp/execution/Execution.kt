package silentorb.imp.execution

import silentorb.imp.core.Connection
import silentorb.imp.core.Graph
import silentorb.imp.core.Id
import silentorb.imp.core.getGraphOutputNode

typealias OutputValues = Map<Id, Any>

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

fun prepareArguments(graph: Graph, outputValues: OutputValues, nodeId: Id): Map<String, Any> {
  throw Error("Not implemented")
//  val values = graph.values
//      .filter { it.node == nodeId }
//      .map { Pair(it.port, it.value) }
//
//  return graph.connections
//      .filter { it.output == nodeId }
//      .map {
//        val rawValue = outputValues[it.input]!!
//        val value = if (it.outPort != null)
//          (rawValue as Map<String, Any>)[it.outPort]!!
//        else
//          rawValue
//
//        Pair(it.port, value)
//      }
//      .plus(values)
//      .associate { it }
}

fun executeNode(graph: Graph, functions: FunctionImplementationMap, values: OutputValues, id: Id): Any {
  val functionName = graph.functions[id]
  val function = functions[functionName]!!
  val arguments = prepareArguments(graph, values, id)
  return function(arguments)
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

fun getGraphOutput(graph: Graph, values: OutputValues): Map<String, Any> {
  val outputNode = getGraphOutputNode(graph)
  return graph.connections
      .filter { it.destination == outputNode }
      .associate { Pair(it.parameter, values[it.source]!!) }
}

fun executeAndFormat(functions: FunctionImplementationMap, graph: Graph): Map<String, Any> {
  val values = execute(functions, graph)
  return getGraphOutput(graph, values)
}
