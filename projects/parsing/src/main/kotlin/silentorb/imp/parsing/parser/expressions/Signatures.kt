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
    functionTypes: Map<Id, PathKey>,
    argumentTypes: Map<Id, PathKey>,
    tokenNodes: Map<TokenIndex, Id>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): Map<Id, FunctionInvocation> {
  return parents.entries
      .filter { (tokenIndex, children) ->
        val id = tokenNodes[tokenIndex]!!
        children.any() && children.all { childIndex -> argumentTypes.containsKey(tokenNodes[childIndex]!!) }
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

typealias SignatureOptions = Map<Id, List<SignatureMatch>>

fun getSignatureOptions(context: Context, aliases: Aliases, invocations: Map<Id, FunctionInvocation>): SignatureOptions {
  return invocations
      .mapValues { (_, invocation) ->
        val functionOverloads = getTypeDetails(context, invocation.type)!!
        overloadMatches(aliases, invocation.arguments, functionOverloads)
      }
}

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val types: Map<Id, PathKey> = mapOf()
)

fun resolveFunctionSignatures(
    context: Context,
    aliases: Aliases,
    tokenGraph: TokenGraph,
    parents: TokenParents,
    functionTypes: Map<Id, PathKey>,
    types: Map<Id, PathKey>,
    tokenNodes: Map<TokenIndex, Id>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): SignatureOptionsAndTypes {
  return tokenGraph.stages.fold(SignatureOptionsAndTypes()) { accumulator, stage ->
    val stageParents = stage.associateWith { parents[it]!! }
    val argumentTypes = types.plus(accumulator.types)
    val invocations = resolveInvocationArguments(stageParents, functionTypes, argumentTypes, tokenNodes, tokens, namedArguments)
    val signatureOptions = getSignatureOptions(context, aliases, invocations)
    val returnTypes = signatureOptions
        .filter { it.value.size == 1 }
        .mapValues { it.value.first().signature.output }
    accumulator.copy(
        signatureOptions = accumulator.signatureOptions.plus(signatureOptions),
        types = accumulator.types.plus(returnTypes)
    )
  }
}
