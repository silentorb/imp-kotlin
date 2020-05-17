package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.NodeMap

data class FunctionInvocation(
    val type: TypeHash,
    val arguments: List<Argument>,
    val range: Range
)

fun getSignatureOptions(context: Context, invocation: FunctionInvocation): List<SignatureMatch> {
  val functionOverloads = flattenTypeSignatures(context)(invocation.type)
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
    argumentTypes: Map<PathKey, TypeHash>,
    nodeMap: NodeMap,
    namedArguments: Map<PathKey, String>
): (Map.Entry<PathKey, List<PathKey>>) -> Pair<PathKey, List<SignatureMatch>>? = { (pathKey, children) ->
//  if (children.all { childIndex -> argumentTypes.containsKey(tokenNodes[childIndex]!!) }
//      && references.containsKey(pathKey)) {
  val functionType = argumentTypes[pathKey]!!
  val arguments = children.map { childNode ->
    Argument(
        name = childNode.name,
        type = argumentTypes[childNode]!!,
        node = childNode
    )
  }
  val invocation = FunctionInvocation(
      type = functionType,
      arguments = arguments,
      range = Range(nodeMap[pathKey]!!.start, nodeMap[children.lastOrNull()]?.end ?: nodeMap[pathKey]!!.end)
  )
  val signatureMatches = getSignatureOptions(context, invocation)
  if (signatureMatches.any())
    Pair(pathKey, signatureMatches)
  else
    null
//  } else
//    null
}

typealias SignatureOptions = Map<PathKey, List<SignatureMatch>>

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val types: Map<PathKey, TypeHash> = mapOf()
)

fun resolveFunctionSignatures(
    context: Context,
    stages: List<List<PathKey>>,
    parents: Map<PathKey, List<PathKey>>,
    initialTypes: Map<PathKey, TypeHash>,
    nodeMap: NodeMap,
    namedArguments: Map<PathKey, String>
): SignatureOptionsAndTypes {
//  val endpointFunctions = references.keys.minus(parents.keys).map { i -> tokenNodes.entries.first { it.value == i }.key }
//  val stages = //listOf(endpointFunctions) +
//      stages
  return stages
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
        val stageParents = stage
            .associateWith { parents[it] ?: listOf() }
        val argumentTypes = initialTypes.plus(accumulator.types)
        val signatureOptions = stageParents
            .mapNotNull(narrowTypeByArguments(context, argumentTypes, nodeMap, namedArguments))
            .associate { it }
        val newTypes = signatureOptions.mapValues { (_, matches) ->
          signaturesToTypeHash(matches.map { it.signature })
        }
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions + signatureOptions,
            types = accumulator.types + newTypes
        )
      }
}
