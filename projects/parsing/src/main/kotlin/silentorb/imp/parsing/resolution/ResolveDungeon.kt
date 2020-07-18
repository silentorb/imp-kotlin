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

fun newDefinitionContext(
    bundles: List<ImportBundle>,
    parentContext: Context): Context {
  val returnTypes = if (bundles.any())
    bundles.map { it.returnTypes }.reduce { a, b -> a + b }
  else
    mapOf()

  return parentContext.plus(
      newNamespace().copy(
          nodeTypes = returnTypes
      )
  )
}

fun newImportedContext(
    namespacePath: String,
    baseContext: Context,
    tokenImports: List<TokenizedImport>,
    sourceContext: Context
): Response<Context> {
  val (rawImportedFunctions, importErrors) = flattenResponses(
      tokenImports
          .map(parseImport(sourceContext))
  )
  val sameNamespaceBundle = ImportBundle(
      returnTypes = sourceContext
          .map { namespace -> namespace.nodeTypes.filterKeys { it.path == namespacePath } }
          .reduce { a, b -> a + b }
  )
  return Response(
      newDefinitionContext(rawImportedFunctions + sameNamespaceBundle, baseContext),
      importErrors
  )
}

fun parseBlock(context: Context, largerContext: Context, definition: DefinitionFirstPass): Response<Dungeon> {
  val definitions = definition.definitions
//      .associateBy { PathKey(formatPathKey(definition.key), it.tokenized.symbol.value as String) }

  val responses = resolveSubDefinitions(context, definitions, largerContext, listOf())
  val dungeons = mergeDungeons(responses.map { it.value })!!
  val errors = responses.flatMap { it.errors }
  return Response(dungeons, errors)
}

tailrec fun resolveSubDefinitions(
    baseContext: Context,
    definitions: List<DefinitionFirstPass>,
    contextAccumulator: Context,
    accumulator: List<Response<Dungeon>>
): List<Response<Dungeon>> =
    if (definitions.none())
      accumulator
    else {
      val next = definitions.first()
      val response = parseDefinitionSecondPass(baseContext, contextAccumulator, next)

      val dungeon = response.value
      resolveSubDefinitions(
          baseContext + dungeon.namespace,
          definitions.drop(1),
          contextAccumulator + dungeon.namespace,
          accumulator + response.copy(errors = response.errors)
      )
    }

tailrec fun resolveDefinitionsRecursive(
    baseContext: Context,
    importMap: Map<Path, List<TokenizedImport>>,
    definitions: List<DefinitionFirstPass>,
    fileContexts: Map<Path, Context>,
    namespaceContexts: Map<String, Context>,
    contextAccumulator: Context,
    accumulator: List<Response<Dungeon>>
): List<Response<Dungeon>> =
    if (definitions.none())
      accumulator
    else {
      val next = definitions.first()
      val file = next.file
      val (context, importErrors) = if (fileContexts.contains(file))
        Response(fileContexts[file]!!, listOf())
      else
        newImportedContext(next.key.path, baseContext, importMap[file]!!, contextAccumulator)

      val namespaceContext = namespaceContexts[next.key.path] ?: listOf()

      val response = parseDefinitionSecondPass(context + namespaceContext, contextAccumulator, next)

      val dungeon = response.value
      val output = getGraphOutputNode(dungeon.namespace)
      val externalGraph = if (output != null)
        newNamespace().copy(
            nodeTypes = dungeon.namespace.nodeTypes.filterKeys { it == output },
            values = dungeon.namespace.values.filterKeys { it == output }
        )
      else
        newNamespace() // Reaching this line is an error but the error should be flagged in other places

      resolveDefinitionsRecursive(
          baseContext,
          importMap,
          definitions.drop(1),
          fileContexts + (file to (context)),
          namespaceContexts + (next.key.path to namespaceContext + externalGraph),
          contextAccumulator + dungeon.namespace,
          accumulator + response.copy(errors = response.errors + importErrors)
      )
    }

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
      val propagations = propagateLiteralTypeAliases(context, mergedDungeon.namespace)
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
