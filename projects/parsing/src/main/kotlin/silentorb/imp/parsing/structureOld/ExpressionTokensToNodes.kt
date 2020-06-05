package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.pathKeyToString
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.parser.expressions.IntermediateExpression
import silentorb.imp.parsing.parser.expressions.resolveLiteralTypes
import silentorb.imp.parsing.parser.expressions.resolveLiterals
import silentorb.imp.parsing.parser.getExpandedChildren
import silentorb.imp.parsing.syntax.BurgType
import silentorb.imp.parsing.syntax.Realm

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
  val parents = realm.roads
//  val parents = collapseNamedArgumentClauses(namedArguments.keys, realm.parents)
//  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val literalTokenKeys = literalTokenNodes(path, realm.burgs.values)
  val nodeReferences = realm.burgs.values.filter { it.type == BurgType.reference }
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
      .mapNotNull { (key, value) ->
        val parent = tokenNodes[key]
        if (parent != null)
          Pair(parent, value.map { tokenNodes[it]!! })
        else
          null
      }
      .associate { it }

  val (stages, dependencyErrors) = arrangeRealm(realm)

  return ParsingResponse(
      IntermediateExpression(
          literalTypes = literalTypes,
          nodeMap = nodeMap,
          parents = pathKeyParents,
          references = nodeReferences
              .groupBy { it.value as String }
              .mapValues { it.value.map { tokenNodes[it.hashCode()]!! }.toSet() },
//          stages = tokenGraph.stages.map { stage -> stage.mapNotNull { tokenNodes[it] } },
          namedArguments = namedArguments.mapKeys { (burg, _) -> tokenNodes[burg]!! },
          stages = stages.map { tokenNodes[it]!! },
          values = literalTokenKeys.entries
              .associate { (burgId, key) ->
                (key to realm.burgs[burgId]!!.value!!)
              }
      ),
      dependencyErrors.map(newParsingError(realm.burgs[realm.root]!!))
  )
}
