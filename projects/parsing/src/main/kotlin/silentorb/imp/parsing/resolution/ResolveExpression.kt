package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.parser.validateFunctionTypes
import silentorb.imp.parsing.parser.validateSignatures

fun resolveExpression(context: Context, largerContext: Context, intermediate: IntermediateExpression): Response<Dungeon> {
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
        val types = getSymbolTypes(context, typeName)
            .entries
            .associate { it.value to it.key }
        val type = typesToTypeHash(types.keys) ?: unknownType.hash
        referenceNodes.map { Pair(it, Pair(type, types)) }
      }
      .associate { it }

  val referenceTypes = referencePairs
      .mapValues { it.value.first }

  val unionTypes = referencePairs
      .filter { it.value.second.size > 1 }.entries
      .associate { (_, value) ->
        value.first to value.second.keys
      }

  val appendedContext = if (unionTypes.any())
    largerContext + newNamespace().copy(typings = newTypings().copy(unions = unionTypes))
  else
    largerContext

  val initialTypes = literalTypes + referenceTypes
  val (signatureOptions, implementationTypes, reducedTypes, typings) =
      resolveFunctionSignatures(
          appendedContext,
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

  val referenceConnections = referencePairs.entries
      .mapNotNull { (key, reference) ->
        val options = reference.second
        val application = applications.entries.firstOrNull { it.value.target == key }
        val signature = signatures[application?.key]?.signature
        val match = options[signature.hashCode()]
        if (match != null)
          Input(key, defaultParameter) to match
        else
          null
      }
      .associate { it }

  val nodeTypes = initialTypes + reducedTypes
  val nonNullaryFunctions = parents.filter { it.value.any() }
  val typeResolutionErrors = validateFunctionTypes(referencePairs.keys, referencePairs.mapValues { it.value.first } + implementationTypes, nodeMap)
  val signatureErrors = validateSignatures(largerContext, nodeTypes, nonNullaryFunctions, signatureOptions, nodeMap)// +
  val errors = signatureErrors + typeResolutionErrors

  val dungeon = emptyDungeon.copy(
      graph = newNamespace().copy(
          connections = connections + referenceConnections,
          implementationTypes = implementationTypes,
          returnTypes = nodeTypes,
          typings = typings,
          values = values
      ),
      nodeMap = nodeMap
  )
  return Response(dungeon, errors)
}
