package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

data class FunctionInvocation(
    val type: PathKey,
    val arguments: List<Argument>,
    val range: Range
)

fun resolveInvocationArguments(
    parents: TokenParents,
    functionTypes: Map<PathKey, PathKey>,
    argumentTypes: Map<PathKey, PathKey>,
    tokenNodes: Map<TokenIndex, PathKey>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): Map<PathKey, FunctionInvocation> {
  return parents.entries
      .filter { (tokenIndex, children) ->
        val id = tokenNodes[tokenIndex]!!
        children.all { childIndex -> argumentTypes.containsKey(tokenNodes[childIndex]!!) }
            && functionTypes.containsKey(id)
      }
      .associate { (tokenIndex, children) ->
        val id = tokenNodes[tokenIndex]!!
        val functionType = functionTypes[id]!!
        val arguments = children.map { childIndex ->
          val childNode = tokenNodes[childIndex]!!
          Argument(
              name = namedArguments[childIndex],
              type = argumentTypes[childNode]!!,
              node = childNode
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

typealias SignatureOptions = Map<PathKey, List<SignatureMatch>>

fun getSignatureOptions(context: Context, invocations: Map<PathKey, FunctionInvocation>): SignatureOptions {
  return invocations
      .mapValues { (_, invocation) ->
        val functionOverloads = getTypeDetails(context, invocation.type)
        if (functionOverloads != null)
          overloadMatches(context, invocation.arguments, functionOverloads)
        else {
          val type = getRootType(context, invocation.type)
          listOf(SignatureMatch(
              signature = Signature(
                  parameters = listOf(),
                  output = type
              ),
              alignment = mapOf()
          ))
        }
      }
}

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val types: Map<PathKey, PathKey> = mapOf()
)

fun resolveFunctionSignatures(
    context: Context,
    tokenGraph: TokenGraph,
    parents: TokenParents,
    functionTypes: Map<PathKey, PathKey>,
    types: Map<PathKey, PathKey>,
    tokenNodes: Map<TokenIndex, PathKey>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): SignatureOptionsAndTypes {
  val endpointFunctions = functionTypes.keys.minus(parents.keys).map { i -> tokenNodes.entries.first { it.value == i }.key }
  val stages = listOf(endpointFunctions) + tokenGraph.stages
  return stages
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
        val stageParents = stage
            .associateWith { parents[it] ?: listOf() }
        val argumentTypes = types.plus(accumulator.types)
        val invocations = resolveInvocationArguments(stageParents, functionTypes, argumentTypes, tokenNodes, tokens, namedArguments)
        val signatureOptions = getSignatureOptions(context, invocations)
        val returnTypes = signatureOptions
            .filter { it.value.size == 1 }
            .mapValues { it.value.first().signature.output }
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions.plus(signatureOptions),
            types = accumulator.types.plus(returnTypes)
        )
      }
}
