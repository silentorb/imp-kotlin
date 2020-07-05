package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.resolution.IntermediateExpression
import silentorb.imp.parsing.resolution.resolveLiteralTypes
import silentorb.imp.parsing.parser.getExpandedChildren
import silentorb.imp.parsing.resolution.FunctionApplication
import silentorb.imp.parsing.syntax.*

fun getNamedArguments(realm: Realm): Map<BurgId, Burg> =
    realm.burgs
        .filterValues { it.type == BurgType.argument }
        .keys
        .mapNotNull { parameter ->
          val children = getExpandedChildren(realm, parameter)
          val argumentName = children.firstOrNull { it.type == BurgType.argumentName }

          val argumentValue = realm.burgs[children.firstOrNull { it.type == BurgType.argumentValue }
              ?.hashCode()]?.children?.firstOrNull()

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
  val applicationsKeys = burgs
      .filter { it.value.type == BurgType.application }
      .keys
      .mapIndexed { index, id -> Pair(id, PathKey(path, "%application${index}")) }
      .associate { it }

  val literalTokenKeys = literalTokenNodes(path, burgs.values)
  val nodeReferences = burgs.values.filter { it.type == BurgType.reference }
  val burgNodes = nodeReferences
      .groupBy { it.value as String }
      .flatMap { (name, burgs) ->
        burgs.mapIndexed { index, burg ->
          Pair(burg.hashCode(), PathKey(path, "$name${index + 1}"))
        }
      }
      .associate { it }
      .plus(literalTokenKeys)
      .plus(applicationsKeys)

  val applications = burgs
      .filter { it.value.type == BurgType.application }
      .mapValues { (_, application) ->
        val children = application.children
            .map { burgs[it]!! }

        val appliedFunction = children
            .first { it.type == BurgType.appliedFunction }
            .children
            .first()

        val arguments = children
            .filter { it.type == BurgType.argument }
            .map { argument ->
              val argumentValueId = argument.children
                  .firstOrNull {
                    burgs[it]!!.type == BurgType.argumentValue
                  }
              assert(argumentValueId != null)
              val argumentValue = burgs[argumentValueId]
              assert(argumentValue!!.children.any())
              val item = argumentValue.children.first()
              item
            }

        FunctionApplication(
            target = burgNodes[appliedFunction]!!,
            arguments = arguments.map { burgNodes[it]!! }
        )
      }
      .mapKeys { applicationsKeys[it.key]!! }

  val parents = applications
      .mapValues { it.value.arguments }

  val nodeMap = burgNodes.entries
      .associate { (id, pathKey) ->
        Pair(pathKey, realm.burgs[id]!!.fileRange)
      }

  val literalTypes = resolveLiteralTypes(realm.burgs, literalTokenKeys)

  val (stages, dependencyErrors) = arrangeRealm(realm)

  return Response(
      IntermediateExpression(
          applications = applications,
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = parents,
          references = nodeReferences
              .groupBy { it.value as String }
              .mapValues { it.value.map { burgNodes[it.hashCode()]!! }.toSet() },
          namedArguments = namedArguments.mapKeys { (burg, _) -> burgNodes[burg]!! },
          stages = stages.mapNotNull { burgNodes[it] }.reversed(),
          values = literalTokenKeys.entries
              .associate { (burgId, key) ->
                (key to realm.burgs[burgId]!!.value!!)
              }
      ),
      dependencyErrors.map(newParsingError(realm.burgs[realm.root]!!))
  )
}
