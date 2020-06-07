package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.resolution.IntermediateExpression
import silentorb.imp.parsing.resolution.resolveLiteralTypes
import silentorb.imp.parsing.parser.getExpandedChildren
import silentorb.imp.parsing.resolution.FunctionApplication
import silentorb.imp.parsing.syntax.BurgType
import silentorb.imp.parsing.syntax.Realm
import silentorb.imp.parsing.syntax.arrangeRealm

fun getNamedArguments(realm: Realm) =
    realm.burgs
        .filterValues { it.type == BurgType.argument }
        .keys.mapNotNull { parameter ->
          val children = getExpandedChildren(realm, parameter)
          val argumentName = children.firstOrNull { it.type == BurgType.argumentName }
          if (argumentName != null)
            (parameter to argumentName)
          else
            null
        }
        .associate { it }

fun expressionTokensToNodes(root: PathKey, realm: Realm): ParsingResponse<IntermediateExpression> {
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
  val tokenNodes = nodeReferences
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
            target = tokenNodes[appliedFunction]!!,
            arguments = arguments.map { tokenNodes[it]!! }
        )
      }
      .mapKeys { applicationsKeys[it.key]!! }

  val parents = applications
      .mapValues { it.value.arguments }

  val nodeMap = tokenNodes.entries
      .associate { (id, pathKey) ->
        Pair(pathKey, realm.burgs[id]!!.fileRange)
      }

  val literalTypes = resolveLiteralTypes(realm.burgs, literalTokenKeys)

  val (stages, dependencyErrors) = arrangeRealm(realm)

  return ParsingResponse(
      IntermediateExpression(
          applications = applications,
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = parents,
          references = nodeReferences
              .groupBy { it.value as String }
              .mapValues { it.value.map { tokenNodes[it.hashCode()]!! }.toSet() },
          namedArguments = namedArguments.mapKeys { (burg, _) -> tokenNodes[burg]!! },
          stages = stages.mapNotNull { tokenNodes[it] }.reversed(),
          values = literalTokenKeys.entries
              .associate { (burgId, key) ->
                (key to realm.burgs[burgId]!!.value!!)
              }
      ),
      dependencyErrors.map(newParsingError(realm.burgs[realm.root]!!))
  )
}
