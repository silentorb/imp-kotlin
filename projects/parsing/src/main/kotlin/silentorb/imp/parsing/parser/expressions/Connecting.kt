package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*

fun arrangeConnections(parents: Map<PathKey, List<PathKey>>, signatures: Map<PathKey, SignatureMatch>): Set<Connection> {
  return parents
      .flatMap { (functionNode, _) ->
        val signatureMatch = signatures[functionNode]
        if (signatureMatch == null)
          listOf()
        else
          signatureMatch.alignment.map { (parameter, sourceNode) ->
            Connection(
                destination = functionNode,
                source = sourceNode,
                parameter = parameter
            )
          }
      }
      .toSet()
}
