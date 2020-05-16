package silentorb.imp.parsing.parser

import silentorb.imp.core.Graph
import silentorb.imp.core.Namespace

// Constraint propagation only travels upward, with later uses applying restrictions to earlier definitions

fun propagateTypeConstraints(namespace: Namespace, graph: Graph): ConstrainedLiteralMap {
  throw Error("Needs updating")
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
////        namespace.numericTypeConstraints.containsKey(type)
//      }
////  val readyConnections = graph.connections
////      .filter { connection ->
////        constraints.contains(connection.destination)
////      }
//  return constraints.associate { it }
}
