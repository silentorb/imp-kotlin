package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.core.Range
import silentorb.imp.core.NodeMap

data class FunctionInvocation(
    val type: TypeHash,
    val arguments: List<Argument>,
    val range: Range
)

fun narrowTypeByArguments(
    context: Context,
    argumentTypes: Map<PathKey, TypeHash>,
    nodeMap: NodeMap,
    namedArguments: Map<PathKey, String>
): (Map.Entry<PathKey, List<PathKey>>) -> Pair<PathKey, List<SignatureMatch>>? = { (pathKey, children) ->
  val functionType = argumentTypes[pathKey]
  if (functionType != null) {
    if (children.any { !argumentTypes.containsKey(it)})
      null
    else {
      val arguments = children
          .map { childNode ->
            Argument(
                name = childNode.name,
                type = argumentTypes[childNode]!!,
                node = childNode
            )
          }
      val functionOverloads = getTypeSignatures(context)(functionType)
      val signatureMatches = overloadMatches(context, arguments, functionOverloads)
      if (signatureMatches.any())
        Pair(pathKey, signatureMatches)
      else
        null
    }
  } else
    null
}

typealias SignatureOptions = Map<PathKey, List<SignatureMatch>>

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val implementationTypes: Map<PathKey, TypeHash> = mapOf(),
    val types: Map<PathKey, TypeHash> = mapOf(),
    val typings: Typings = newTypings()
)

fun resolveFunctionSignatures(
    context: Context,
    stages: List<PathKey>,
    parents: Map<PathKey, List<PathKey>>,
    initialTypes: Map<PathKey, TypeHash>,
    nodeMap: NodeMap,
    namedArguments: Map<PathKey, String>
): SignatureOptionsAndTypes {
  return stages
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
//        val stageParents = stage
//            .filter { parents[it]?.any() ?: false }
//            .associateWith { parents[it] ?: listOf() }
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = context + newNamespace().copy(typings = accumulator.typings)
        val signatureOptions = stageParents
            .mapNotNull(narrowTypeByArguments(newContext, argumentTypes, nodeMap, namedArguments))
            .associate { it }
        val reducedNestedSignatures = signatureOptions.mapValues { (_, matches) ->
          matches.map { reduceSignature(it.signature, it.alignment.keys) }
        }
        val newImplementationTypes = signatureOptions.mapValues { (_, matches) ->
          signaturesToTypeHash(matches.map { it.signature })
        }
        val newTypes = reducedNestedSignatures
            .filter { it.value.size == 1 }
            .mapValues { (_, signatures) ->
              signatures.first().output
            }
        val newTypings = reduceTypings(reducedNestedSignatures.values.map(::extractTypings))
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions + signatureOptions,
            implementationTypes = accumulator.implementationTypes + newImplementationTypes,
            types = accumulator.types + newTypes,
            typings = accumulator.typings + newTypings
        )
      }
}
