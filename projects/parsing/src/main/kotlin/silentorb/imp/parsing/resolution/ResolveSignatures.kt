package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.parsing.syntax.Burg
import silentorb.mythic.debugging.getDebugBoolean

fun narrowTypeByArguments(
    context: Context,
    largerContext: Context,
    argumentTypes: Map<PathKey, TypeHash>,
    children: List<PathKey>,
    types: Map<PathKey, TypeHash>,
    namedArguments: Map<PathKey, Burg>
): List<SignatureMatch> {
  return if (children.any { !argumentTypes.containsKey(it) })
    listOf()
  else {
    val arguments = children
        .map { childNode ->
          Argument(
              name = namedArguments[childNode]?.value as String? ?: childNode.name,
              type = argumentTypes[childNode]!!,
              node = childNode
          )
        }

    val functionOverloads = types.mapNotNull {
      val signature = getTypeSignature(largerContext, it.value)
      if (signature == null)
        null
      else
        Pair(it.key, signature)
    }
        .associate { it }
    val signatureMatches = overloadMatches(largerContext, arguments, functionOverloads)
    if (signatureMatches.any()) {
      signatureMatches.filter { it.complete } // TODO: Temporary until currying is supported. Then just prioritize complete.
    } else {
      if (getDebugBoolean("IMP_PARSER_DEBUG_SIGNATURE_MISMATCHES")) {
        val typeNames = types.values.map { getTypeNameOrNull(largerContext, it) }
        val argumentTypeNames = arguments.map { getTypeNameOrNull(largerContext, it.type) }
        val overloadTypeNames = functionOverloads.map { overload ->
          overload.value.parameters.map {
            getTypeNameOrNull(largerContext, it.type)
          }
        }
        val k = 0
      }
      listOf()
    }
  }
}

typealias SignatureOptions = Map<PathKey, List<SignatureMatch>>

data class SignatureOptionsAndTypes(
    val signatureOptions: SignatureOptions = mapOf(),
    val types: Map<PathKey, TypeHash> = mapOf(),
    val typings: Typings = newTypings()
)

fun resolveFunctionSignatures(
    namespaceContext: Context,
    largerContext: Context,
    stages: List<PathKey>,
    referenceOptions: Map<PathKey, Map<PathKey, TypeHash>>,
    applications: Map<PathKey, FunctionApplication>,
    initialTypes: Map<PathKey, TypeHash>,
    namedArguments: Map<PathKey, Burg>
): SignatureOptionsAndTypes {
  return stages
      .filter { node -> referenceOptions.containsKey(node) }
      .fold(SignatureOptionsAndTypes()) { accumulator, node ->
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = largerContext + newNamespace().copy(typings = accumulator.typings)
        val application = applications.entries.firstOrNull { it.value.target == node }
        val arguments = if (application != null)
          application.value.arguments
        else
          listOf()

        val types = referenceOptions[node]
        val signatureOptions = if (types != null && types.any())
          narrowTypeByArguments(
              namespaceContext,
              newContext,
              argumentTypes,
              arguments,
              types,
              namedArguments
          )
        else
          listOf()

        val reducedNestedSignatures = signatureOptions.map { reduceSignature(it.signature, it.alignment.keys) }

        val newTypes = if (signatureOptions.size == 1)
          mapOf(
              node to signatureOptions.first().signature.hashCode()
          ).plus(
              if (application != null)
                mapOf(application.key to signatureOptions.first().signature.output)
              else
                mapOf()
          )
        else
          mapOf()

        val newTypings = extractGroupedTypings(reducedNestedSignatures)
        accumulator.copy(
            signatureOptions = accumulator.signatureOptions + (node to signatureOptions),
            types = accumulator.types + newTypes,
            typings = accumulator.typings + newTypings
        )
      }
}
