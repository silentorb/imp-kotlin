package silentorb.imp.execution

import silentorb.imp.core.*

typealias OutputValues = Map<PathKey, Any>
typealias Arguments = Map<String, Any>

tailrec fun getNodeDependencyConnections(context: Context, nodes: Set<PathKey>, accumulator: Connections): Connections =
    if (nodes.none())
      accumulator
    else {
      val newConnections = nodes
          .map { getInputConnections(context, it) }
          .reduce { a, b -> a + b }

      val newNodes = newConnections.values
          .toSet()
          .minus(accumulator.keys.map { it.destination })

      getNodeDependencyConnections(context, newNodes, accumulator + newConnections)
    }

fun arrangeGraphSequence(context: Context, connections: Connections): List<PathKey> {
  val nodes = connections.values
      .plus(connections.keys.map { it.destination })
      .filter {
        val type = getReturnType(context, it)
        if (type != null)
          getTypeSignatures(context)(type).all { it.parameters.none() }
        else
          false
      }
      .toSet()

  val dependencies = connections
      .map {
        Dependency(
            dependent = it.key.destination,
            provider = it.value
        )
      }
      .toSet()

  return arrangeDependencies(nodes, dependencies).first
}

fun generateNodeFunction(context: Context,
                         functions: FunctionImplementationMap,
                         node: PathKey
): NodeImplementation {
  val reference = resolveReference(context, node) ?: node
  val k = getPathKeyImplementationTypes(context, reference)
  val j = getPathKeyImplementationTypes(context, node)
  val implementationType = getReturnType(context, reference) ?: getReturnType(context, node)
  val signature = if (implementationType != null) getTypeSignature(context, implementationType) else null
  val value = getValue(context, node)
  if (value != null) {
    return { _: NodeImplementationArguments ->
      value
    }
  } else if (implementationType == null && reference != node) {
    return generateNodeFunction(context, functions, reference)
  } else if (implementationType != null && signature != null) {
    if (signature.parameters.none()) {
      return { values: NodeImplementationArguments ->
        values[reference]!!
      }
    }
    else {
      val implementationNode = resolveReference(context, reference)!!
      val implementationKey = FunctionKey(implementationNode, implementationType)
      val function = functions[implementationKey]!!
      val argumentKeys = getArgumentConnections(context, node)
      return { values: NodeImplementationArguments ->
        function(argumentKeys.entries.associate { it.key.parameter to values[it.value]!! })
      }
    }
  } else
    throw Error("Insufficient data to execute node $node")
}

fun executeStep(): (OutputValues, ExecutionStep) -> OutputValues = { values, step ->
  values + (step.node to step.execute(values))
}

fun executeStep(values: OutputValues, step: ExecutionStep) =
    values + (step.node to step.execute(values))

fun executeSteps(steps: List<ExecutionStep>, values: OutputValues): OutputValues {
  return steps.fold(values) { accumulator, step -> executeStep(accumulator, step) }
}

fun prepareExecutionSteps(
    context: Context,
    functions: FunctionImplementationMap,
    resultNodes: Set<PathKey>): List<ExecutionStep> {
  val connections = getNodeDependencyConnections(context, resultNodes, mapOf())
  val steps = arrangeGraphSequence(context, connections)
  return steps.map { node ->
    ExecutionStep(
        node = node,
        execute = generateNodeFunction(context, functions, node)
    )
  }
}

fun execute(
    context: Context,
    functions: FunctionImplementationMap,
    nodes: Set<PathKey>
): OutputValues {
  val steps = prepareExecutionSteps(context, functions, nodes)
  return executeSteps(steps, mapOf())
}

fun executeToSingleValue(
    context: Context,
    functions: FunctionImplementationMap,
    graph: Graph
): Any? {
  val output = getGraphOutputNode(graph)
  return if (output == null)
    null
  else {
    val values = execute(context + graph, functions, setOf(output))
    values[output]
  }
}

fun mergeImplementationFunctions(context: Context, implementationGraphs: Map<FunctionKey, Graph>, functions: FunctionImplementationMap): FunctionImplementationMap {
  var newFunctions: FunctionImplementationMap = mapOf()
  newFunctions = functions + getImplementationFunctions(context, implementationGraphs) { newFunctions }
  return newFunctions
}

fun executeToSingleValue(context: Context, functions: FunctionImplementationMap, dungeon: Dungeon): Any? {
  val combinedContext = context + dungeon.graph
  val newFunctions = mergeImplementationFunctions(combinedContext, dungeon.implementationGraphs, functions)
  return executeToSingleValue(context, functions + newFunctions, dungeon.graph)
}
