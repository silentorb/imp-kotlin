package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.resolution.IntermediateExpression
import silentorb.imp.parsing.resolution.resolveLiteralTypes
import silentorb.imp.parsing.resolution.FunctionApplication
import silentorb.imp.parsing.syntax.*

fun getNamedArguments(realm: Realm): Map<Burg, Burg> =
    realm.burgs
        .filter { it.type == BurgType.argument }
        .mapNotNull { parameter ->
          val children = parameter.children
          val argumentName = children.firstOrNull { it.type == BurgType.burgName }

          val argumentValue = children
              .firstOrNull { it.type == BurgType.argumentValue }
              ?.children
              ?.firstOrNull()

          if (argumentName != null && argumentValue != null) {
            (argumentValue to argumentName)
          } else
            null
        }
        .associate { it }

fun expressionTokensToNodes(root: PathKey, realm: Realm): Response<IntermediateExpression> {
  val path = pathKeyToString(root)
  val namedArguments = getNamedArguments(realm)
  val burgs = realm.burgs
  val applicationKeys = burgs
      .filter { it.type == BurgType.application }
      .mapIndexed { index, id -> Pair(id, PathKey(path, "%application${index}")) }
      .associate { it }

  val literalTokenKeys = literalTokenNodes(path, burgs)
  val nodeReferences = burgs.filter { it.type == BurgType.reference }
  val burgNodes = nodeReferences
      .groupBy { it.value as String }
      .flatMap { (name, burgs) ->
        burgs.mapIndexed { index, burg ->
          Pair(burg, PathKey(path, "$name${index + 1}"))
        }
      }
      .associate { it }
      .plus(literalTokenKeys)
      .plus(applicationKeys)

  val applications = burgs
      .filter { it.type == BurgType.application }
      .associateWith { application ->
        val children = application.children

        val appliedFunction = children.first()

        val arguments = children
            .drop(1)
            .map { argument ->
              val argumentValue = argument.children
                  .firstOrNull {
                    it.type == BurgType.argumentValue
                  }?.children?.firstOrNull()
                  ?: if (argument.type == BurgType.argument)
                    argument.children.first()
                  else
                    argument

              argumentValue
            }

        assert(arguments.all { burgNodes.containsKey(it) })
        FunctionApplication(
            target = burgNodes[appliedFunction]!!,
            arguments = arguments.map { burgNodes[it]!! }
        )
      }
      .mapKeys { applicationKeys[it.key]!! }

  val parents = applications
      .mapValues { it.value.arguments }

  val nodeMap = burgNodes.entries
      .associate { (id, pathKey) ->
        Pair(pathKey, id.fileRange)
      }

  val literalTypes = resolveLiteralTypes(literalTokenKeys)

  val (stages, dependencyErrors) = arrangeRealm(realm)

  return Response(
      IntermediateExpression(
          applications = applications,
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = parents,
          references = nodeReferences
              .groupBy { it.value as String }
              .flatMap { reference -> reference.value.map { burgNodes[it]!! to reference.key } }
              .associate { it },
          namedArguments = namedArguments.mapKeys { (burg, _) -> burgNodes[burg]!! },
          stages = stages.mapNotNull { burgNodes[it] }.reversed(),
          values = literalTokenKeys.entries
              .associate { (burgId, key) ->
                (key to burgId.value!!)
              }
      ),
      dependencyErrors.map(newParsingError(realm.root))
  )
}
