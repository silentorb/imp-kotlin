package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.*
import silentorb.imp.parsing.structureOld.parseDefinitionFirstPass
import java.nio.file.Path

fun gatherTypeNames(context: Context, nodeTypes: Map<PathKey, TypeHash>): Map<TypeHash, String> =
    nodeTypes
        .values
        .distinct()
        .mapNotNull { type ->
          val key = getTypeNameOrNull(context, type)
          if (key == null)
            null
          else
            Pair(type, key)
        }
        .associate { it }

fun resolveDefinitions(importMap: Map<Path, List<TokenizedImport>>, tokenDefinitions: Map<PathKey, TokenizedDefinition>, baseContext: Context, largerContext: Context): Response<List<Dungeon>> {
  val firstPass = tokenDefinitions
      .mapValues { (pathKey, definition) ->
        parseDefinitionFirstPass(pathKey, definition)
      }

  val firstPassErrors = firstPass.values
      .flatMap { it.errors }

  val definitions = firstPass
      .mapNotNull { it.value.value }

  val definitionMap = definitions.associateBy { it.key }
  val dependencies = definitions
      .flatMap { definition ->
        definition.definitions
            .mapNotNull { it.intermediate }
            .plus(listOfNotNull(definition.intermediate))
            .flatMap { intermediate ->
              intermediate.references.values
                  .mapNotNull { referenceName ->
                    val provider = definitionMap.keys.firstOrNull { it.name == referenceName }
                    if (provider != null)
                      Dependency(
                          dependent = definition.key,
                          provider = provider
                      )
                    else
                      null
                  }
            }
      }
      .toSet()

  val (arrangedDefinitionKeys, dependencyErrors) = arrangeDependencies(definitionMap.keys, dependencies)

  // Keep it as a lambda so that the burg resolution code is not called unless there are items in the list
  val dependencyParsingErrors = dependencyErrors.map { newParsingError(tokenDefinitions.values.first().symbol)(it) }
  val arrangedDefinitions = arrangedDefinitionKeys.map { definitionMap[it]!! }
  val resolutions = resolveDefinitionsRecursive(baseContext, importMap, arrangedDefinitions, mapOf(), mapOf(), largerContext, listOf())
  val result = flattenResponses(resolutions)
  return result
      .copy(
          errors = result.errors + firstPassErrors + dependencyParsingErrors
      )
}

fun finalizeDungeons(context: Context, nodeRanges: Map<PathKey, TokenizedDefinition>): (List<Dungeon>) -> Response<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.fileRange }

      val initialGraph = newNamespace().copy(
          connections = mapOf(),
          values = mapOf()
      )

      val initialDungeon = emptyDungeon.copy(
          namespace = initialGraph,
          nodeMap = nodeMap
      )

      val mergedDungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
        mergeDungeons(a, expressionDungeon)
      }
      val flattenedContext = listOf(mergeNamespaces(context))
      val propagations = propagateLiteralTypeAliases(flattenedContext, mergedDungeon.namespace)
      val dungeon = mergedDungeon.copy(
          namespace = mergedDungeon.namespace.copy(
              nodeTypes = mergedDungeon.namespace.nodeTypes + propagations
          )
      )
      val constraintErrors = validateTypeConstraints(dungeon.namespace.values, context, propagations, dungeon.nodeMap)
      val typeNames = gatherTypeNames(context, dungeon.namespace.nodeTypes)

      Response(
          dungeon
              .copy(
                  namespace = dungeon.namespace.copy(
                      typings = dungeon.namespace.typings.copy(
                          typeNames = dungeon.namespace.typings.typeNames + typeNames
                      )
                  )
              ),
          constraintErrors
      )
    }

fun parseDungeon(context: Context, importMap: Map<Path, List<TokenizedImport>>, definitions: Map<PathKey, TokenizedDefinition>): Response<Dungeon> {
  val baseContext = listOf(newNamespace())
  val (dungeons, definitionErrors) = resolveDefinitions(importMap, definitions, baseContext, context)
  val importErrors = validateUnusedImports(context, importMap, definitions)
  val (dungeon, dungeonErrors) = finalizeDungeons(context, definitions)(dungeons)
  return Response(
      dungeon,
      definitionErrors + dungeonErrors + importErrors
  )
}
