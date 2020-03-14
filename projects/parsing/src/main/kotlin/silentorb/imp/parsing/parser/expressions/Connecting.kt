package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*

fun arrangeConnections(parents: TokenParents, tokenNodes: Map<TokenIndex, Id>, signatures: Map<Id, SignatureMatch>): Set<Connection> {
  return parents
      .flatMap { (tokenIndex, _) ->
        val functionNode = tokenNodes[tokenIndex]!!
        val signatureMatch = signatures[functionNode]
        if (signatureMatch == null)
          listOf()
        else
          signatureMatch.alignment.map {(sourceNode, parameter) ->
            Connection(
                destination = functionNode,
                source = sourceNode,
                parameter = parameter
            )
          }
      }
      .toSet()
}
