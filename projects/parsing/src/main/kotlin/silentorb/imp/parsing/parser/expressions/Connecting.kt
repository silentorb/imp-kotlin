package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*

fun getParametersByType(parameters: List<Parameter>, type: PathKey): List<Parameter> =
    parameters.filter { it.type == type }

fun arrangeConnections(parents: TokenParents, tokenNodes: Map<TokenIndex, Id>, signatures: SignatureMap,
                       namedArguments: Map<Int, String>,
                       types: Map<Id, PathKey>): Set<Connection> {
  return parents
      .flatMap { (tokenIndex, children) ->
        val functionNode = tokenNodes[tokenIndex]!!
        val signature = signatures[functionNode]
        children.mapIndexed { index, childIndex ->
          val sourceNode = tokenNodes[childIndex]!!
          val parameter = namedArguments[childIndex]
              ?: signature?.parameters?.filter { it.type == types[sourceNode] }?.getOrNull(index)?.name
              ?: ""

          Connection(
              destination = functionNode,
              source = sourceNode,
              parameter = parameter
          )
        }
      }
      .toSet()
}
