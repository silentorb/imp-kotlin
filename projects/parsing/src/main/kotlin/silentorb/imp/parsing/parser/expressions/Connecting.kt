package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*

fun arrangeConnections(parents: TokenParents, tokenNodes: Map<TokenIndex, Id>, signatures: SignatureMap,
                       namedArguments: Map<Int, String>,
                       types: Map<Id, PathKey>): Set<Connection> {
  return parents
      .flatMap { (tokenIndex, children) ->
        val functionNode = tokenNodes[tokenIndex]!!
        val signature = signatures[functionNode]
        if (signature == null)
          listOf()
        else
          children
              .groupBy { types[tokenNodes[it]!!] }
              .flatMap { (type, children) ->
                val parameters = signature.parameters.filter { it.type == type }
                children.mapIndexed { index, childIndex ->
                  val sourceNode = tokenNodes[childIndex]!!
                  val parameter = namedArguments[childIndex]
                      ?: parameters.getOrNull(index)?.name
                      ?: throw Error("Logic for matching signatures and connecting nodes is not aligned.")
                  Connection(
                      destination = functionNode,
                      source = sourceNode,
                      parameter = parameter
                  )
                }
              }
      }
      .toSet()
}
