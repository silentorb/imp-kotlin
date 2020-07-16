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
    val types = getSymbolTypes(context, pathKey.name)
    val functionOverloads = types.mapNotNull { getTypeSignature(largerContext, it.value) }
    val signatureMatches = overloadMatches(context, arguments, functionOverloads)
    if (signatureMatches.any()) {
      signatureMatches.filter { it.complete } // TODO: Temporary until currying is supported. Then just prioritize complete.
    } else {
      if (getDebugBoolean("IMP_PARSER_DEBUG_SIGNATURE_MISMATCHES")) {
        val argumentTypeNames = arguments.map { getTypeNameOrNull(context, it.type) }
        val overloadTypeNames = functionOverloads.map { overload ->
          overload.parameters.map {
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
    stages: List<PathKey>,
    applications: Map<PathKey, FunctionApplication>,
    initialTypes: Map<PathKey, TypeHash>,
    namedArguments: Map<PathKey, Burg>
): SignatureOptionsAndTypes {
  return stages
      .filter { node -> !initialTypes.containsKey(node) && applications.none { node == it.value.target } }
      .fold(SignatureOptionsAndTypes()) { accumulator, node ->
        val argumentTypes = initialTypes.plus(accumulator.types)
        val newContext = largerContext + newNamespace().copy(typings = accumulator.typings)
        val application = applications[node]
        if (application != null) {
          if (application.arguments.none() && !initialTypes.containsKey(application.target)) {
            val type = argumentTypes[application.target] ?: getPathKeyTypes(namespaceContext, application.target).firstOrNull()
            if (type != null) {
              accumulator.copy(
                  signatureOptions = accumulator.signatureOptions + (node to listOf(
                      SignatureMatch(
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
                application.target,
                application.arguments,
                namedArguments
            )
            val reducedNestedSignatures = signatureOptions.map { reduceSignature(it.signature, it.alignment.keys) }

            val newTypes = if (signatureOptions.size == 1)
              mapOf(
                  application.target to signatureOptions.first().signature.hashCode(),
                  node to signatureOptions.first().signature.output
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
