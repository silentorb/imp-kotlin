package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.parser.validateFunctionTypes
import silentorb.imp.parsing.parser.validateSignatures
import silentorb.imp.parsing.structureOld.arrangeRealm

fun parseExpression(context: Context, largerContext: Context, intermediate: IntermediateExpression): ParsingResponse<Dungeon> {
  val (
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
          parents,
          initialTypes,
          nodeMap,
          namedArguments
      )
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, signatures)
  val nodeTypes = initialTypes + reducedTypes
  val nonNullaryFunctions = parents.filter { it.value.any() }
  val typeResolutionErrors = validateFunctionTypes(nodeMap.keys, initialTypes, nodeMap)
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
