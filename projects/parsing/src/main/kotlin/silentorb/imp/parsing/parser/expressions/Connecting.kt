package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.Connection
import silentorb.imp.core.Id
import silentorb.imp.core.SignatureMap

fun arrangeConnections(parents: TokenParents, tokenNodes: Map<TokenIndex, Id>, signatures: SignatureMap,
                       namedArguments: Map<Int, String>): Set<Connection> {
  return parents
      .flatMap { (tokenIndex, children) ->
        val functionNode = tokenNodes[tokenIndex]!!
        val signature = signatures[functionNode]
        children.mapIndexed { index, childIndex ->
          Connection(
              destination = functionNode,
              source = tokenNodes[childIndex]!!,
              parameter = namedArguments[childIndex] ?: signature?.parameters?.get(index)?.name ?: ""
          )
        }
      }
      .toSet()
}
