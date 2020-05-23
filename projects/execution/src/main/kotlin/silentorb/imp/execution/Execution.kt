package silentorb.imp.execution

import silentorb.imp.core.*

typealias OutputValues = Map<PathKey, Any>
typealias Arguments = Map<String, Any>

fun nextStage(nodes: Set<PathKey>, connections: Collection<Connection>, references: Map<PathKey, PathKey>): List<PathKey> {
  return nodes.filter { node -> connections.none { it.destination == node } && nodes.none { references[node] == it } }
      .map { it }
}

fun arrangeGraphStages(graph: Graph): List<List<PathKey>> {
  var nodes = graph.nodes
  var connections = graph.connections
  var references = graph.references
  var result = listOf<List<PathKey>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections, references)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.source) }.toSet()
    references = references.filter { !nextNodes.contains(it.value) }
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
    val reference = graph.references[id]
    val type = graph.implementationTypes[id]
    if (reference == null) {
      val arguments = prepareArguments(graph, values, id)
      assert(arguments.size == 1)
      arguments.values.first()
    } else if (type != null) {
      val function = functions[FunctionKey(reference, type)]!!
      val arguments = prepareArguments(graph, values, id)
      function(if (additionalArguments != null) arguments.plus(additionalArguments) else arguments)
    } else if (values.containsKey(reference)) {
      values[reference]!!
    } else
      throw Error("Insufficient data to execute node $id")
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

fun executeToSingleValue(functions: FunctionImplementationMap, graph: Graph): Any? {
  val result = execute(functions, graph)
  val output = getGraphOutputNode(graph)
  return if (output == null)
    null
  else
    result[output]
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
