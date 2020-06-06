package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.parser.expressions.IntermediateExpression
import silentorb.imp.parsing.parser.expressions.resolveLiteralTypes
import silentorb.imp.parsing.parser.getExpandedChildren
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
  val parents = burgs
      .filter { it.value.type == BurgType.application }
      .mapValues { (_, application) ->
        application.children.filter { burgs[it]!!.type == BurgType.argument }
      }
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

  val nodeMap = tokenNodes.entries
      .associate { (id, pathKey) ->
        Pair(pathKey, realm.burgs[id]!!.fileRange)
      }

  val literalTypes = resolveLiteralTypes(realm.burgs, literalTokenKeys)

  val pathKeyParents = parents.entries
      .associate { (key, value) ->
        val application = burgs[key]!!
        val appliedFunction = burgs.entries.first {
          it.value.type == BurgType.appliedFunction && application.children.contains(it.key)
        }
        Pair(tokenNodes[appliedFunction.key]!!, value.map { tokenNodes[it]!! })
      }

  val (stages, dependencyErrors) = arrangeRealm(realm)

  return ParsingResponse(
      IntermediateExpression(
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = pathKeyParents,
          references = nodeReferences
              .groupBy { it.value as String }
              .mapValues { it.value.map { tokenNodes[it.hashCode()]!! }.toSet() },
          namedArguments = namedArguments.mapKeys { (burg, _) -> tokenNodes[burg]!! },
          stages = stages.mapNotNull { tokenNodes[it] },
          values = literalTokenKeys.entries
              .associate { (burgId, key) ->
                (key to realm.burgs[burgId]!!.value!!)
              }
      ),
      dependencyErrors.map(newParsingError(realm.burgs[realm.root]!!))
  )
}
