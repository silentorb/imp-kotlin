package silentorb.imp.execution

import silentorb.imp.core.*
import silentorb.imp.parsing.parser.Dungeon

typealias OutputValues = Map<PathKey, Any>
typealias Arguments = Map<String, Any>

fun nextStage(nodes: Set<PathKey>, connections: Connections): List<PathKey> =
    nodes
        .filter { node -> connections.none { it.key.destination == node } }
        .map { it }

fun arrangeGraphStages(graph: Graph, values: OutputValues): List<List<PathKey>> {
  var nodes = graph.nodes
      .minus(graph.values.keys)
      .minus(values.keys)
      .filter { graph.typings.signatures[graph.returnTypes[it]]?.parameters?.none() ?: true }
      .toSet()

  // Only grab connections that connect the filtered pool of execution nodes
  var connections = graph.connections
      .filter { nodes.contains(it.key.destination) && nodes.contains(it.value) }
  var result = listOf<List<PathKey>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.value) }
  }

  return result
}

fun arrangeGraphSequence(graph: Graph, values: OutputValues): List<PathKey> =
    arrangeGraphStages(graph, values).flatten()

fun prepareArguments(graph: Graph, outputValues: OutputValues, destination: PathKey): Arguments {
  return graph.connections
      .filter { it.key.destination == destination && it.key.parameter != defaultParameter }
      .entries
      .associate {
        val value = outputValues[it.value]!!
        Pair(it.key.parameter, value)
      }
}

fun executeNode(graph: Graph, functions: FunctionImplementationMap, values: OutputValues, node: PathKey,
                additionalArguments: Arguments? = null): Any {
  val reference = graph.connections[Input(node, defaultParameter)]
  val type = graph.implementationTypes[node]
  return if (reference == null) {
    val arguments = prepareArguments(graph, values, node)
    assert(arguments.size == 1)
    arguments.values.first()
  } else if (type != null) {
    val implementationKey = FunctionKey(reference, type)
    val function = functions[implementationKey]!!
    val arguments = prepareArguments(graph, values, node)
    function(if (additionalArguments != null) arguments + additionalArguments else arguments)
  } else if (values.containsKey(reference)) {
    values[reference]!!
  } else
    throw Error("Insufficient data to execute node $node")
}

fun executeStep(functions: FunctionImplementationMap, graph: Graph): (OutputValues, PathKey) -> OutputValues = { values, node ->
  values.plus(node to executeNode(graph, functions, values, node))
}

fun executeStep(functions: FunctionImplementationMap, graph: Graph, values: OutputValues, node: PathKey, additionalArguments: Arguments) =
    values.plus(node to executeNode(graph, functions, values, node, additionalArguments))

fun execute(functions: FunctionImplementationMap, graph: Graph, steps: List<PathKey>, values: OutputValues): OutputValues {
  return steps.fold(values, executeStep(functions, graph))
}

fun execute(functions: FunctionImplementationMap, graph: Graph, values: OutputValues): OutputValues {
  val steps = arrangeGraphSequence(graph, values)
  return execute(functions, graph, steps, graph.values + values)
}

fun executeToSingleValue(functions: FunctionImplementationMap, graph: Graph, values: OutputValues = mapOf()): Any? {
  val result = execute(functions, graph, values)
  val output = getGraphOutputNode(graph)
  return if (output == null)
    null
  else
    result[output]
}

fun getImplementationFunctions(dungeon: Dungeon, functions: FunctionImplementationMap): FunctionImplementationMap {
  val graph = dungeon.graph
  return dungeon.implementationGraphs.mapValues { (key, functionGraph) ->
    val signature = graph.typings.signatures[key.type]!!
    val parameters = signature.parameters
    { arguments: Arguments ->
      val values = parameters.associate {
        Pair(PathKey(pathKeyToString(key.key), it.name), arguments[it.name]!!)
      }
      executeToSingleValue(functions, functionGraph, values)!!
    }
  }
}

fun executeToSingleValue(functions: FunctionImplementationMap, dungeon: Dungeon): Any? {
  val newFunctions = getImplementationFunctions(dungeon, functions)
  return executeToSingleValue(functions + newFunctions, dungeon.graph)
}
