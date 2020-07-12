package silentorb.imp.parsing.parser

import silentorb.imp.core.*

// Constraint propagation only travels upward, with later uses applying restrictions to earlier definitions

fun propagateLiteralTypeAliases(context: Context, graph: Graph): Map<PathKey, TypeHash> {
  val propagations = graph.values.keys
      .mapNotNull { id ->
        val connections = graph.connections.filter { it.value == id }
        val types = connections
            .mapNotNull { connection ->
              val functionType = graph.returnTypes[connection.key.destination]
              if (functionType != null)
                getTypeSignature(context, functionType)?.parameters?.firstOrNull { it.name == connection.key.parameter }?.type
              else
                null
            }
            .distinct()
        if (types.size == 1)
          Pair(id, types.first())
        else
          null
      }
      .associate { it }

  return propagations
//  return mapOf() // TODO: Uncomment below code
//  val constraints = graph.connections
//      .filter {}
//  val constraints = graph.nodes
//      .flatMap { node ->
//        graph.signatureMatches
//            .flatMap { (_, signatureMatch) ->
//              signatureMatch.alignment.entries
//                  .filter { it.value == node }
//                  .map { Pair(it.value, signatureMatch.signature.parameters.first { parameter -> parameter.name == it.key }.type) }
//            }
//            .filter { (_, type) ->
//              namespace.numericTypeConstraints.containsKey(type)
//            }
//      }
//
//  return constraints.associate { it }
}
