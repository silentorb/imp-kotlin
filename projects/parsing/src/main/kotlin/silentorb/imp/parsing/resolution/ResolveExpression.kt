package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.parser.validateFunctionTypes
import silentorb.imp.parsing.parser.validateSignatures

fun resolveExpression(
    context: Context,
    largerContext: Context,
    intermediate: IntermediateExpression
): Response<Dungeon> {
  val (
      applications,
      literalTypes,
      namedArguments,
      nodeMap,
      parents,
      references,
      stages,
      values
  ) = intermediate

  val referenceOptions = references.mapValues { (_, target) ->
    getSymbolTypes(context, target)
  }

  val initialTypes = literalTypes
  val (signatureOptions, reducedTypes, typings) =
      resolveFunctionSignatures(
          context,
          largerContext,
          stages,
          referenceOptions,
          applications,
          initialTypes,
          namedArguments
      )
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, applications, signatures)
      .plus(
          applications.map { (key, application) ->
            Input(
                destination = key,
                parameter = defaultParameter
            ) to application.target
          }
      )

  val referenceConnections = referenceOptions.keys
      .mapNotNull { key ->
        val options = signatureOptions[key] ?: listOf()
        if (options.none()) {
          null
        } else {
          Input(key, defaultParameter) to options.first().key!!
        }
      }
      .associate { it }

  val nodeTypes = initialTypes + reducedTypes
  val referenceValues = referenceConnections
      .filter { it.key.parameter == defaultParameter }
      .mapNotNull { (destination, source) ->
        val nodeType = nodeTypes[destination.destination]
        val referenceValue = getValue(largerContext, source.copy(type = nodeType)) ?: getValue(largerContext, source)
        if (referenceValue != null)
          destination.destination to referenceValue
        else
          null
      }
      .associate { it }

  val typeResolutionErrors = validateFunctionTypes(referenceOptions, nodeMap)
  val signatureErrors = validateSignatures(largerContext, nodeTypes, referenceOptions.filter { it.value.any() }.keys, parents, signatureOptions, applications, nodeMap)
  val errors = signatureErrors + typeResolutionErrors

//  if (referenceConnections.size == referencePairs.size || errors.any()) {
//    val k = 0
//  }
  val temp = nodeTypes.values
      .filter { type -> getTypeSignature(largerContext, type) ?: typings.signatures[type] != null }
  assert(temp.size == nodeTypes.size || errors.any())

  val dungeon = emptyDungeon.copy(
      namespace = newNamespace().copy(
          connections = connections + referenceConnections,
          nodeTypes = nodeTypes,
          typings = typings,
          values = values + referenceValues
      ),
      nodeMap = nodeMap
  )
  return Response(dungeon, errors)
}
