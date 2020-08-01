package silentorb.imp.parsing.parser

import silentorb.imp.core.*

// Constraint propagation only travels upward, with later uses applying restrictions to earlier definitions

tailrec fun gatherLiteralDependents(
    context: Context,
    namespace: Namespace,
    connections: Map<Input, PathKey>,
    accumulator: Set<TypeHash>
): Set<TypeHash> =
    if (connections.none())
      accumulator
    else {
      val types = connections
          .filter { it.key.parameter != defaultParameter }
          .mapNotNull { connection ->
            val functionTarget = namespace.connections[Input(connection.key.destination, defaultParameter)]
            if (functionTarget != null) {
              val functionType = namespace.nodeTypes[functionTarget]
              if (functionType != null) {
                val signature = getTypeSignature(context, functionType)
                signature
                    ?.parameters
                    ?.firstOrNull { it.name == connection.key.parameter }
                    ?.type
              } else
                null
            } else
              null
          }

      val passThroughConnections = connections
          .filter { it.key.parameter == defaultParameter }

      val nextConnections = namespace.connections
          .filter { connection -> passThroughConnections.any { it.key.destination == connection.value } }

      gatherLiteralDependents(context, namespace, nextConnections, accumulator + types)
    }

fun propagateLiteralTypeAliases(context: Context, namespace: Namespace): Map<PathKey, TypeHash> {
  val propagations = namespace.values
      .filter { !isFunction(it.value) }
      .keys
      .mapNotNull { literalNode ->
        val initialType = namespace.nodeTypes[literalNode]
        val connections = namespace.connections.filter { it.value == literalNode }
        val types = gatherLiteralDependents(context, namespace, connections, setOf())
            .filter { it != initialType }
        if (types.any())
          Pair(literalNode, types.first())
        else
          null
      }
      .associate { it }

  return propagations
}
