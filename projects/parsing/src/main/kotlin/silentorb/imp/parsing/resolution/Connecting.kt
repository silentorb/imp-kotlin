package silentorb.imp.parsing.resolution

import silentorb.imp.core.*

fun arrangeConnections(
    parents: Map<PathKey, List<PathKey>>,
    applications: Map<PathKey, FunctionApplication>,
    signatures: Map<PathKey, SignatureMatch>
): Connections {
  return parents
      .flatMap { (node, _) ->
        val application = applications[node]
        val functionNode = if (application!= null)
          application.target
        else
          node

        val signatureMatch = signatures[functionNode]
        if (signatureMatch == null)
          listOf()
        else
          signatureMatch.alignment.map { (parameter, sourceNode) ->
            Input(
                destination = node,
                parameter = parameter
            ) to sourceNode
          }
      }
      .associate { it }
}
