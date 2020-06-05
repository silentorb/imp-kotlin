package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.core.Range

data class FunctionInvocation(
    val type: TypeHash,
    val arguments: List<Argument>,
    val range: Range
)

fun narrowTypeByArguments(
    context: Context,
    argumentTypes: Map<PathKey, TypeHash>,
    pathKey: PathKey,
    children: List<PathKey>
): List<SignatureMatch> {
  val functionType = argumentTypes[pathKey]
  return if (functionType != null) {
    if (children.any { !argumentTypes.containsKey(it) })
      listOf()
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
        signatureMatches
      else
        listOf()
    }
  } else
    listOf()
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
    initialTypes: Map<PathKey, TypeHash>
): SignatureOptionsAndTypes {
  return stages
      .filter { parents[it]?.any() ?: false }
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
        val inputs = parents[stage]!!
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = context + newNamespace().copy(typings = accumulator.typings)
        val signatureOptions = narrowTypeByArguments(newContext, argumentTypes, stage, inputs)
        val reducedNestedSignatures = signatureOptions.map { reduceSignature(it.signature, it.alignment.keys) }
        val newImplementationTypes = signaturesToTypeHash(signatureOptions.map { it.signature })
        val newTypes = if (signatureOptions.size == 1)
          mapOf(stage to signatureOptions.first().signature.output)
        else
          mapOf()

        val newTypings = extractTypings(reducedNestedSignatures)
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions + (stage to signatureOptions),
            implementationTypes = accumulator.implementationTypes + (stage to newImplementationTypes),
            types = accumulator.types + newTypes,
            typings = accumulator.typings + newTypings
        )
      }
}
