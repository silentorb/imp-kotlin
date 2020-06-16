package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.structureOld.parseDefinitionFirstPass
import java.nio.file.Path

fun gatherTypeNames(context: Context, nodeTypes: Map<PathKey, TypeHash>): Map<TypeHash, PathKey> =
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

fun newImportedContext(
    namespacePath: String,
    baseContext: Context,
    tokenImports: List<TokenizedImport>,
    sourceContext: Context
): ParsingResponse<Context> {
  val (rawImportedFunctions, importErrors) = flattenResponses(
      tokenImports
          .map(parseImport(sourceContext))
  )
  val sameNamespaceBundle = ImportBundle(
      implementationTypes = sourceContext
          .map { namespace -> namespace.implementationTypes.filterKeys { it.path == namespacePath } }
          .reduce { a, b -> a + b },
      returnTypes = sourceContext
          .map { namespace -> namespace.returnTypes.filterKeys { it.path == namespacePath } }
          .reduce { a, b -> a + b }
  )
  return ParsingResponse(
      newDefinitionContext(rawImportedFunctions + sameNamespaceBundle, baseContext),
      importErrors
  )
}

tailrec fun resolveDefinitions(
    defaultContext: Context,
    importMap: Map<Path, List<TokenizedImport>>,
    definitions: List<DefinitionFirstPass>,
    fileContexts: Map<Path, Context>,
    namespaceContexts: Map<String, Context>,
    contextAccumulator: Context,
    accumulator: List<ParsingResponse<Dungeon>>
): List<ParsingResponse<Dungeon>> =
    if (definitions.none())
      accumulator
    else {
      val next = definitions.first()
      val file = next.file
      val (context, importErrors) = if (fileContexts.contains(file))
        ParsingResponse(fileContexts[file]!!, listOf())
      else
        newImportedContext(next.key.path, defaultContext, importMap[file]!!, contextAccumulator)

      val namespaceContext = namespaceContexts[next.key.path] ?: listOf()

      val response = parseDefinitionSecondPass(context + namespaceContext, contextAccumulator, next)
      val dungeon = response.value
      resolveDefinitions(
          defaultContext,
          importMap,
          definitions.drop(1),
          fileContexts + (file to (context)),
          namespaceContexts + (next.key.path to namespaceContext + dungeon.graph),
          contextAccumulator + dungeon.graph,
          accumulator + response.copy(errors = response.errors + importErrors)
      )
    }

fun resolveDefinitions(importMap: Map<Path, List<TokenizedImport>>, tokenDefinitions: Map<PathKey, TokenizedDefinition>, context: Context): ParsingResponse<List<Dungeon>> {
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

  // Keep it as a lambda so that the burg resolution code is not called unless there are items in the list
  val dependencyParsingErrors = dependencyErrors.map { newParsingError(tokenDefinitions.values.first().symbol)(it) }
  val defaultContext = listOf(newNamespace())
  val arrangedDefinitions = arrangedDefinitionKeys.map { definitionMap[it]!! }
  val resolutions = resolveDefinitions(defaultContext, importMap, arrangedDefinitions, mapOf(), mapOf(), context, listOf())
  val result = flattenResponses(resolutions)
  return result
      .copy(
          errors = result.errors + firstPassErrors + dependencyParsingErrors
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

fun parseDungeon(context: Context, importMap: Map<Path, List<TokenizedImport>>, definitions: Map<PathKey, TokenizedDefinition>): ParsingResponse<Dungeon> {
  val (dungeons, definitionErrors) = resolveDefinitions(importMap, definitions, context)
  val importErrors = validateUnusedImports(context, importMap, definitions)
  val (dungeon, dungeonErrors) = finalizeDungeons(context, definitions)(dungeons)
  return ParsingResponse(
      dungeon,
      definitionErrors + dungeonErrors + importErrors
  )
}
