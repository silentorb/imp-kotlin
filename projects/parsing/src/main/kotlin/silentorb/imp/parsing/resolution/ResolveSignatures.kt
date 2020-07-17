package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.parsing.syntax.Burg
import silentorb.mythic.debugging.getDebugBoolean

fun narrowTypeByArguments(
    context: Context,
    largerContext: Context,
    argumentTypes: Map<PathKey, TypeHash>,
    pathKey: PathKey,
    children: List<PathKey>,
    referenceOptions: Map<PathKey, Map<PathKey, TypeHash>>,
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
    val types = referenceOptions[pathKey]!!
    val functionOverloads = types.mapNotNull {
      val signature = getTypeSignature(largerContext, it.value)
      if (signature ==null)
        null
      else
        Pair(it.key, signature)
    }
        .associate { it }
    val signatureMatches = overloadMatches(context, arguments, functionOverloads)
    if (signatureMatches.any()) {
      signatureMatches.filter { it.complete } // TODO: Temporary until currying is supported. Then just prioritize complete.
    } else {
      if (getDebugBoolean("IMP_PARSER_DEBUG_SIGNATURE_MISMATCHES")) {
        val typeNames = types.values.map { getTypeNameOrNull(context, it) }
        val argumentTypeNames = arguments.map { getTypeNameOrNull(context, it.type) }
        val overloadTypeNames = functionOverloads.map { overload ->
          overload.value.parameters.map {
            getTypeNameOrNull(context, it.type)
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
    parents: Map<PathKey, List<PathKey>>,
    stages: List<PathKey>,
    referenceOptions: Map<PathKey, Map<PathKey, TypeHash>>,
    initialTypes: Map<PathKey, TypeHash>,
    namedArguments: Map<PathKey, Burg>
): SignatureOptionsAndTypes {
  return stages
      .filter { node -> !initialTypes.containsKey(node) }
      .fold(SignatureOptionsAndTypes()) { accumulator, node ->
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = largerContext + newNamespace().copy(typings = accumulator.typings)
        val arguments = parents[node]
        if (arguments != null) {
          if (arguments.none() && initialTypes.containsKey(node)) {
            val type = argumentTypes[node] ?: getPathKeyTypes(namespaceContext, node).firstOrNull()
            if (type != null) {
              accumulator.copy(
                  signatureOptions = accumulator.signatureOptions + (node to listOf(
                      SignatureMatch(
                          key = null,
                          signature = Signature(parameters = listOf(), output = type, isVariadic = false),
                          alignment = mapOf(),
                          complete = true
                      )
                  )),
                  types = accumulator.types + (node to type)
              )
            } else
              accumulator
          } else {
            val signatureOptions = narrowTypeByArguments(
                namespaceContext,
                newContext,
                argumentTypes,
                node,
                arguments,
                referenceOptions,
                namedArguments
            )
            val reducedNestedSignatures = signatureOptions.map { reduceSignature(it.signature, it.alignment.keys) }

            val newTypes = if (signatureOptions.size == 1)
              mapOf(
                  node to signatureOptions.first().signature.hashCode()
//                  node to signatureOptions.first().signature.output
              )
            else
              mapOf()

            val newTypings = extractTypings(reducedNestedSignatures)
            accumulator.copy(
                signatureOptions = accumulator.signatureOptions + (node to signatureOptions),
                types = accumulator.types + newTypes,
                typings = accumulator.typings + newTypings
            )
          }
        } else { // Nullary reference
          val types = getSymbolTypes(namespaceContext, node.name)
          val type = types.values.firstOrNull()
          if (type == null)
            accumulator
          else {
            accumulator.copy(
                types = accumulator.types + (node to type)
            )
          }
        }
      }
}
