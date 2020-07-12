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
                         node: PathKey
): NodeImplementation {
  val nodeType = getReturnType(context, node)
      ?: throw Error("Missing nodeType for ${formatPathKey(node)}")

  if (getTypeUnion(context, nodeType) != null)
    return { }

  val value = getValue(context, node.copy(type = nodeType)) ?: getValue(context, node)

  if (value == null) {
    val inputs = getInputConnections(context, node).entries
    val target = inputs.firstOrNull { it.key.parameter == defaultParameter }
        ?: throw Error("Missing application node type for ${formatPathKey(node)}")

    val targetNode = target.value
    if (inputs.size > 1) {
      val targetType = getReturnType(context, targetNode)
          ?: throw Error("Missing nodeType for ${formatPathKey(node)}")

      val signature = getTypeSignature(context, targetType)
          ?: throw Error("Missing signature for ${formatPathKey(node)}")

      val function = getValue(context, targetNode) as FunctionImplementation
      val argumentInputs = inputs.minus(target)
      if (signature.isVariadic) {
        return { values: NodeImplementationArguments ->
          val list = argumentInputs.map { values[it.value]!! }
          function(mapOf("values" to list))
        }
      } else if (signature.parameters.none()) {
        return { function(mapOf()) }
      } else {
        return { values: NodeImplementationArguments ->
          function(argumentInputs.associate { it.key.parameter to values[it.value]!! })
        }
      }
    } else {
      return { values: NodeImplementationArguments ->
        values[targetNode]!!
      }
//      throw Error("Not yet supported")
    }
  } else {
    return { value }
  }
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
        execute = generateNodeFunction(context, node)
    )
  }
}

fun execute(
    context: Context,
    functions: FunctionImplementationMap,
    nodes: Set<PathKey>
): OutputValues {
  val steps = prepareExecutionSteps(listOf(mergeNamespaces(context)), functions, nodes)
  return executeSteps(steps, mapOf())
}

fun executeToSingleValue(
    context: Context,
    functions: FunctionImplementationMap,
    namespace: Namespace
): Any? {
  val output = getGraphOutputNode(namespace)
  return if (output == null)
    null
  else {
    val values = execute(context + namespace, functions, setOf(output))
    values[output]
  }
}

fun prepareExecutionUnit(
    context: Context,
    functions: FunctionImplementationMap,
    output: PathKey
): ExecutionUnit {

  return ExecutionUnit(
      steps = prepareExecutionSteps(context, functions, setOf(output)),
      values = mapOf(),
      output = output
  )
}

fun executeToSingleValue(unit: ExecutionUnit): Any? {
  val values = executeSteps(unit.steps, unit.values)
  return values[unit.output]!!
}

fun mergeImplementationFunctions(context: Context, implementationGraphs: Map<FunctionKey, Namespace>, functions: FunctionImplementationMap): FunctionImplementationMap {
  var newFunctions: FunctionImplementationMap = mapOf()
  newFunctions = functions + getImplementationFunctions(context, implementationGraphs) { newFunctions }
  return newFunctions
}

fun executeToSingleValue(context: Context, functions: FunctionImplementationMap, dungeon: Dungeon): Any? {
  val combinedContext = context + dungeon.namespace
  val newFunctions = mergeImplementationFunctions(combinedContext, dungeon.implementationGraphs, functions)
  return executeToSingleValue(context, functions + newFunctions, dungeon.namespace)
}

fun executeToSingleValue(context: Context, functions: FunctionImplementationMap, root: PathKey): Any? {
  val values = execute(context, functions, setOf(root))
  return values[root]
}
