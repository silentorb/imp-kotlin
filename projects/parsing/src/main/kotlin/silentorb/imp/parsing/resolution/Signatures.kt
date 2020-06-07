package silentorb.imp.parsing.resolution

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
    applications: Map<PathKey, FunctionApplication>,
    initialTypes: Map<PathKey, TypeHash>
): SignatureOptionsAndTypes {
  return stages
      .filter { applications.keys.contains(it) }
      .fold(SignatureOptionsAndTypes()) { accumulator, stage ->
        val application = applications[stage]!!
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = context + newNamespace().copy(typings = accumulator.typings)
        if (application.arguments.none()) {
          val type = argumentTypes[application.target]
          if (type != null) {
            accumulator.copy(
                types = accumulator.types + (stage to type)
            )
          } else
            accumulator
        } else {
          val signatureOptions = narrowTypeByArguments(newContext, argumentTypes, application.target, application.arguments)
          val reducedNestedSignatures = signatureOptions.map { reduceSignature(it.signature, it.alignment.keys) }
          val newImplementationTypes = if (signatureOptions.any())
            mapOf(stage to signaturesToTypeHash(signatureOptions.map { it.signature }))
          else
            mapOf()

          val newTypes = if (signatureOptions.size == 1)
            mapOf(stage to signatureOptions.first().signature.output)
          else
            mapOf()

          val newTypings = extractTypings(reducedNestedSignatures)
          accumulator.copy(
              signatureOptions = accumulator.signatureOptions + (stage to signatureOptions),
              implementationTypes = accumulator.implementationTypes + newImplementationTypes,
              types = accumulator.types + newTypes,
              typings = accumulator.typings + newTypings
          )
        }
      }
}
