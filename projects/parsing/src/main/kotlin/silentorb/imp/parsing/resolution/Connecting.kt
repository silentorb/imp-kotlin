package silentorb.imp.parsing.resolution

import silentorb.imp.core.*

fun arrangeConnections(parents: Map<PathKey, List<PathKey>>, signatures: Map<PathKey, SignatureMatch>): Connections {
  return parents
      .flatMap { (functionNode, _) ->
        val signatureMatch = signatures[functionNode]
        if (signatureMatch == null)
          listOf()
        else
          signatureMatch.alignment.map { (parameter, sourceNode) ->
            (Input(
                destination = functionNode,
                parameter = parameter
            ) to sourceNode)
          }
      }
      .associate { it }
}
