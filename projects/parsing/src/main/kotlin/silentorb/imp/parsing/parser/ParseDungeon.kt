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

tailrec fun resolveDefinitions(definitions: List<DefinitionFirstPass>, context: Context, accumulator: List<ParsingResponse<Dungeon>>): List<ParsingResponse<Dungeon>> =
    if (definitions.none())
      accumulator
    else {
      val next = definitions.first()
      val response = parseDefinitionSecondPass(context, next)
      val dungeon = response.value
      resolveDefinitions(definitions.drop(1), context + dungeon.graph, accumulator + response)
    }

fun parseDefinitions(nodeRanges: Map<PathKey, TokenizedDefinition>, initialContext: Context): ParsingResponse<List<Dungeon>> {
  val firstPass = nodeRanges
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
  val resolutions = resolveDefinitions(arrangedDefinitions, initialContext, listOf())
  val result = flattenResponses(resolutions)
  return result
      .copy(
          errors = result.errors + firstPassErrors + dependencyErrors.map { newParsingError(TextId.circularDependency, nodeRanges.values.first().symbol) }
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

fun parseDungeon(parentContext: Context): (TokenizedGraph) -> ParsingResponse<Dungeon> =
    { (imports, definitions) ->
      val (rawImportedFunctions, importErrors) = flattenResponses(imports.map(parseImport(parentContext)))
      val nodeRanges = definitions.associateBy { PathKey(localPath, it.symbol.value) }
      val baseContext = listOf(
          newNamespace()
              .copy(
                  typings = parentContext.map { it.typings }.reduce(::mergeTypings)
              )
      )
      val context = newDefinitionContext(rawImportedFunctions, baseContext)
      val (dungeons, definitionErrors) = parseDefinitions(nodeRanges, context)
      val (dungeon, dungeonErrors) = finalizeDungeons(context, nodeRanges)(dungeons)
      ParsingResponse(
          dungeon,
          importErrors + definitionErrors + dungeonErrors
      )
    }
