package silentorb.imp.parsing.parser

import silentorb.imp.core.Graph
import silentorb.imp.core.Namespace

// Constraint propagation only travels upward, with later uses applying restrictions to earlier definitions

fun propagateTypeConstraints(namespace: Namespace, graph: Graph): ConstrainedLiteralMap {
  return mapOf() // TODO: Uncomment below code
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
