package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

fun gatherTypeNames(context: Context, nodeTypes: Map<PathKey, TypeHash>) =
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

fun newDefinitionContext(
    bundles: List<ImportBundle>,
    parentContext: Context): Context {
  val (returnTypes, implementationTypes) = if (bundles.any())
    Pair(
        bundles.map { it.returnTypes }.reduce { a, b -> a + b },
        bundles.map { it.implementationTypes }.reduce { a, b -> a + b }
    )
  else
    Pair(mapOf(), mapOf())

  return parentContext.plus(
      newNamespace().copy(
          returnTypes = returnTypes,
          implementationTypes = implementationTypes
      )
  )
}

tailrec fun resolveDefinitions(definitions: List<DefinitionFirstPass>, contexts: Map<PathKey, Context>, contextAccumulator: Context, accumulator: List<ParsingResponse<Dungeon>>): List<ParsingResponse<Dungeon>> =
    if (definitions.none())
      accumulator
    else {
      val next = definitions.first()
      val context = contexts[pathKeyFromString(next.key.path)]!!
      val response = parseDefinitionSecondPass(context + contextAccumulator, next)
      val dungeon = response.value
      resolveDefinitions(definitions.drop(1), contexts, contextAccumulator + dungeon.graph, accumulator + response)
    }

fun parseDefinitions(tokenDefinitions: Map<PathKey, TokenizedDefinition>, contexts: Map<PathKey, Context>): ParsingResponse<List<Dungeon>> {
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
        definition.intermediate.references.keys
            .mapNotNull { referenceName ->
              val provider = definitionMap.keys.firstOrNull() { it.name == referenceName }
              if (provider != null)
                Dependency(
                    dependent = definition.key,
                    provider = provider
                )
              else
                null
            }
      }
      .toSet()

  val (arrangedDefinitionKeys, dependencyErrors) = arrangeDependencies(definitionMap.keys, dependencies)

  val arrangedDefinitions = arrangedDefinitionKeys.map { definitionMap[it]!! }
  val resolutions = resolveDefinitions(arrangedDefinitions, contexts, listOf(newNamespace()), listOf())
  val result = flattenResponses(resolutions)
  return result
      .copy(
          errors = result.errors + firstPassErrors + dependencyErrors.map { newParsingError(TextId.circularDependency, tokenDefinitions.values.first().symbol) }
      )
}

fun finalizeDungeons(context: Context, nodeRanges: Map<PathKey, TokenizedDefinition>): (List<Dungeon>) -> ParsingResponse<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.fileRange }

      val initialGraph = newNamespace().copy(
          connections = mapOf(),
          values = mapOf()
      )

      val initialDungeon = emptyDungeon.copy(
          graph = initialGraph,
          nodeMap = nodeMap
      )

      val mergedDungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
        mergeDungeons(a, expressionDungeon)
      }
      val propagations = propagateLiteralTypeAliases(context, mergedDungeon.graph)
      val dungeon = mergedDungeon.copy(
          graph = mergedDungeon.graph.copy(
              returnTypes = mergedDungeon.graph.returnTypes + propagations
          )
      )
      val constraintErrors = validateTypeConstraints(dungeon.graph.values, context, propagations, dungeon.nodeMap)
      val typeNames = gatherTypeNames(context, dungeon.graph.returnTypes)

      ParsingResponse(
          dungeon
              .copy(
                  graph = dungeon.graph.copy(
                      typings = dungeon.graph.typings.copy(
                          typeNames = dungeon.graph.typings.typeNames + typeNames
                      )
                  )
              ),
          constraintErrors
      )
    }

fun parseDungeon(parentContext: Context, graphs: Map<PathKey, TokenGraph>, fileNamespaces: Boolean = false): ParsingResponse<Dungeon> {
//  val imports = graph.imports
//  val definitions = graph.definitions
  val baseContext = listOf(
      newNamespace()
          .copy(
              typings = parentContext.map { it.typings }.reduce(::mergeTypings)
          )
  )
  val (contexts, importErrors) = flattenResponseMap(
      graphs.mapValues { (_, tokenGraph) ->
        val (rawImportedFunctions, importErrors) = flattenResponses(tokenGraph.imports.map(parseImport(parentContext)))
        ParsingResponse(
            newDefinitionContext(rawImportedFunctions, baseContext),
            importErrors
        )
      }
//          .mapKeys { joinPaths(it.key.path, it.key.name).drop(1) }
  )

  val definitions = graphs.entries
      .flatMap { (pathKey, tokenGraph) ->
        val path = if (fileNamespaces)
          pathKeyToString(pathKey)
        else
          pathKey.path

        tokenGraph.definitions.map { PathKey(path, it.symbol.value) to it }
      }
      .associate { it }

  val (dungeons, definitionErrors) = parseDefinitions(definitions, contexts)
  val (dungeon, dungeonErrors) = finalizeDungeons(contexts.values.flatten(), definitions)(dungeons)
  return ParsingResponse(
      dungeon,
      importErrors + definitionErrors + dungeonErrors
  )
}
