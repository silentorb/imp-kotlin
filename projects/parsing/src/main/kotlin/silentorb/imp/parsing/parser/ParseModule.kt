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

fun finalizeDungeons(context: Context, nodeRanges: Map<PathKey, TokenizedDefinition>): (List<Dungeon>) -> ParsingResponse<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.fileRange }

      val initialGraph = newNamespace().copy(
//          nodes = nodeRanges.keys,
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

fun addGraphToContext(graph: Graph, context: Context): Context =
    context
        .dropLast(1)
        .plus(context.last() + graph)

fun parseDefinitions(nodeRanges: Map<PathKey, TokenizedDefinition>, initialContext: Context): ParsingResponse<List<Dungeon>> {
  val (dungeonResponses) = nodeRanges.entries
      .fold<Map.Entry<PathKey, TokenizedDefinition>, Pair<List<ParsingResponse<Dungeon>>, Context>>(Pair(listOf(), initialContext)) { a, b ->
        val (responses, context) = a
        val response = parseDefinition(context)(b)
        val nextContext = addGraphToContext(response.value.graph, context)
        Pair(responses.plus(response), nextContext)
      }

  return flattenResponses(dungeonResponses)
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
