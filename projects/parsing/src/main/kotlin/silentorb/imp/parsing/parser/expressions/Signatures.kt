package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

data class FunctionInvocation(
    val type: PathKey,
    val arguments: List<Argument>,
    val range: Range
)

fun getSignatureOptions(context: Context, invocation: FunctionInvocation): List<SignatureMatch> {
  val functionOverloads = getTypeSignatures(context, invocation.type)
//  if (functionOverloads != null)
    return overloadMatches(context, invocation.arguments, functionOverloads)
//  else {
//    val type = getRootType(context, invocation.type)
//    listOf(SignatureMatch(
//        signature = Signature(
//            parameters = listOf(),
//            output = type
//        ),
//        alignment = mapOf()
//    ))
//  }
}

fun narrowTypeByArguments(
    context: Context,
    references: Map<PathKey, PathKey>,
    argumentTypes: Map<PathKey, TypeHash>,
    tokenNodes: Map<TokenIndex, PathKey>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): (Map.Entry<TokenIndex, List<TokenIndex>>) -> Pair<PathKey, List<SignatureMatch>>? = { (tokenIndex, children) ->
  val id = tokenNodes[tokenIndex]!!
  if (children.all { childIndex -> argumentTypes.containsKey(tokenNodes[childIndex]!!) }
      && references.containsKey(id)) {
    val functionType = references[id]!!
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
    val signatureMatches = getSignatureOptions(context, invocation)
    Pair(id, signatureMatches)
  } else
    null
}

typealias SignatureOptions = Map<PathKey, List<SignatureMatch>>

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val types: Map<PathKey, TypeHash> = mapOf()
)

fun resolveFunctionSignatures(
    context: Context,
    tokenGraph: TokenGraph,
    parents: TokenParents,
    references: Map<PathKey, PathKey>,
    types: Map<PathKey, TypeHash>,
    tokenNodes: Map<TokenIndex, PathKey>,
    tokens: Tokens,
    namedArguments: Map<Int, String>
): SignatureOptionsAndTypes {
  val endpointFunctions = references.keys.minus(parents.keys).map { i -> tokenNodes.entries.first { it.value == i }.key }
  val stages = listOf(endpointFunctions) + tokenGraph.stages
  return stages
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
        val stageParents = stage
            .associateWith { parents[it] ?: listOf() }
        val argumentTypes = types.plus(accumulator.types)
        val signatureOptions = stageParents.mapNotNull(narrowTypeByArguments(context, references, argumentTypes, tokenNodes, tokens, namedArguments))
            .associate { it }
//        val signatureOptions = getSignatureOptions(context, invocations)
//        val returnTypes = signatureOptions
//            .filter { it.value.size == 1 }
//            .mapValues { it.value.first().signature.output }
        val newTypes = signatureOptions.mapValues { (_, matches) ->
          signaturesToTypeHash(matches.map { it.signature })
        }
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions + signatureOptions,
            types = accumulator.types + newTypes
        )
      }
}
