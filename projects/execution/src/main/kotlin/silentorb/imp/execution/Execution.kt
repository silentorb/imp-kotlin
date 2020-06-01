package silentorb.imp.execution

import silentorb.imp.core.*
import silentorb.imp.core.Dungeon

typealias OutputValues = Map<PathKey, Any>
typealias Arguments = Map<String, Any>

fun arrangeGraphSequence(graph: Graph, values: OutputValues): List<PathKey> {
  val nodes = graph.nodes
      .minus(graph.values.keys)
      .minus(values.keys)
      .filter { graph.typings.signatures[graph.returnTypes[it]]?.parameters?.none() ?: true }
      .toSet()

  // Only grab connections that connect the filtered pool of execution nodes
  val dependencies = graph.connections
      .filter { nodes.contains(it.key.destination) && nodes.contains(it.value) }
      .map {
        Dependency(
            dependent = it.key.destination,
            provider = it.value
        )
      }
      .toSet()

  return arrangeDependencies(nodes, dependencies).first
}

fun executeNode(
    graph: Graph,
    functions: FunctionImplementationMap,
    values: OutputValues,
    node: PathKey,
    additionalArguments: Arguments? = null
): Any {
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

fun executeStep(
    functions: FunctionImplementationMap,
    graph: Graph
): (OutputValues, PathKey) -> OutputValues = { values, node ->
  values.plus(node to executeNode(graph, functions, values, node))
}

fun executeStep(
    functions: FunctionImplementationMap,
    graph: Graph,
    values: OutputValues,
    node: PathKey,
    additionalArguments: Arguments
) =
    values.plus(node to executeNode(graph, functions, values, node, additionalArguments))

fun execute(
    functions: FunctionImplementationMap,
    graph: Graph,
    steps: List<PathKey>,
    values: OutputValues
): OutputValues {
  return steps.fold(values, executeStep(functions, graph))
}

fun execute(
    functions: FunctionImplementationMap,
    graph: Graph,
    values: OutputValues
): OutputValues {
  val steps = arrangeGraphSequence(graph, values)
  return execute(functions, graph, steps, graph.values + values)
}

fun executeToSingleValue(
    functions: FunctionImplementationMap,
    graph: Graph,
    values: OutputValues = mapOf()
): Any? {
  val result = execute(functions, graph, values)
  val output = getGraphOutputNode(graph)
  return if (output == null)
    null
  else
    result[output]
}

fun mergeImplementationFunctions(context: Context, implementationGraphs: Map<FunctionKey, Graph>, functions: FunctionImplementationMap): FunctionImplementationMap {
  var newFunctions: FunctionImplementationMap = mapOf()
  newFunctions = functions + getImplementationFunctions(context, implementationGraphs) { newFunctions }
  return newFunctions
}

fun executeToSingleValue(context: Context, functions: FunctionImplementationMap, dungeon: Dungeon): Any? {
  val combinedContext = context + dungeon.graph
  val newFunctions = mergeImplementationFunctions(combinedContext, dungeon.implementationGraphs, functions)
  return executeToSingleValue(functions + newFunctions, dungeon.graph)
}
