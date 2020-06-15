package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.parser.validateFunctionTypes
import silentorb.imp.parsing.parser.validateSignatures

fun parseExpression(context: Context, largerContext: Context, intermediate: IntermediateExpression): ParsingResponse<Dungeon> {
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

  val referencePairs = references
      .flatMap { (typeName, referenceNodes) ->
        val type = getSymbolType(context, typeName)
            ?: unknownType.hash
        val targetKey = resolveReference(context, typeName) ?: unknownType.key
        referenceNodes.map { Pair(it, Pair(type, targetKey)) }
      }
      .associate { it }

  val referenceTypes = referencePairs
      .mapValues { it.value.first }

  val referenceKeys = referencePairs
      .mapValues { it.value.second }

  val initialTypes = literalTypes + referenceTypes
  val (signatureOptions, implementationTypes, reducedTypes, typings) =
      resolveFunctionSignatures(
          largerContext,
          stages,
          applications,
          initialTypes,
          namedArguments
      )
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, signatures)
      .plus(
          applications.map { (key, application) ->
            Input(
                destination = key,
                parameter = defaultParameter
            ) to application.target
          }
      )
  val nodeTypes = initialTypes + reducedTypes
  val nonNullaryFunctions = parents.filter { it.value.any() }
//  val typeResolutionErrors = validateFunctionTypes(applications.filter { it.value.arguments.any() }.keys, implementationTypes, nodeMap)
  val typeResolutionErrors = validateFunctionTypes(referencePairs.keys, referencePairs.mapValues { it.value.first } + implementationTypes, nodeMap)
  val signatureErrors = validateSignatures(largerContext, nodeTypes, nonNullaryFunctions, signatureOptions, nodeMap)// +
  val errors = signatureErrors + typeResolutionErrors

  val dungeon = emptyDungeon.copy(
      graph = newNamespace().copy(
          connections = connections + referenceKeys.entries.associate { Input(it.key, defaultParameter) to it.value },
          implementationTypes = implementationTypes,
          returnTypes = nodeTypes,
          typings = typings,
          values = values
      ),
      nodeMap = nodeMap
  )
  return ParsingResponse(dungeon, errors)
}
