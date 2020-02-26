package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.matchFunction

data class FunctionInvocation(
    val type: PathKey,
    val arguments: List<Argument>,
    val range: Range
)

fun resolveInvocationArguments(
    parents: TokenParents,
    types: Map<Id, PathKey>,
    tokenNodes: Map<TokenIndex, Id>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): Map<Id, FunctionInvocation> {
  return parents.entries
      .filter { it.value.any() }
      .associate { (tokenIndex, children) ->
        val id = tokenNodes[tokenIndex]!!
        val functionType = types[id]!!
        val arguments = children.mapIndexed { index, childIndex ->
          val childNode = tokenNodes[childIndex]!!
          Argument(
              name = namedArguments[childIndex],
              type = types[childNode]!!
          )
        }
        val invocation = FunctionInvocation(
            type = functionType,
            arguments = arguments,
            range = tokensToRange(listOf(tokens[tokenIndex]).plus(children.map { tokens[it] }))
        )
        Pair(id, invocation)
      }
}

fun resolveSignatures(context: Context, invocations: Map<Id, FunctionInvocation>): PartitionedResponse<SignatureMap> {
  return partitionMap(invocations
      .mapValues { (_, invocation) ->
        val functionOverloads = getTypeDetails(context, invocation.type)!!
        matchFunction(invocation.arguments, functionOverloads, invocation.range)
      }
  )
}
