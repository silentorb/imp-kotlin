package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.parseExpression

data class TokenizedImport(
    val path: Tokens
)

data class TokenizedParameter(
    val name: String,
    val type: String
)

data class TokenizedDefinition(
    val symbol: Token,
    val parameters: List<TokenizedParameter>,
    val expression: Tokens
)

data class TokenizedGraph(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

fun parseDefinition(context: Context): (Map.Entry<PathKey, TokenizedDefinition>) -> PartitionedResponse<Dungeon> =
    { (id, definition) ->
      val tokens = definition.expression.filter { it.rune != Rune.newline }
      if (tokens.none()) {
        PartitionedResponse(
            Dungeon(
                graph = newNamespace(),
                nodeMap = mapOf()
            ),
            listOf(newParsingError(TextId.missingExpression, definition.symbol))
        )
      } else {
        val matchingParenthesesErrors = checkMatchingParentheses(tokens)
        val path = PathKey(localPath, definition.symbol.value)
        val (dungeon, expressionErrors) = parseExpression(path, context, tokens)
        val output = getGraphOutputNode(dungeon.graph)
        val nextDungeon = if (output != null)
          dungeon.copy(
              graph = dungeon.graph.copy(
                  connections = dungeon.graph.connections + Connection(
                      source = output,
                      destination = id,
                      parameter = defaultParameter
                  ),
                  nodeTypes = dungeon.graph.nodeTypes + (path to dungeon.graph.nodeTypes[output]!!)
              )
          )
        else
          dungeon

//      val outputType = nextDungeon.graph.outputTypes[output]
        PartitionedResponse(
//          if (outputType != null)
//            nextDungeon.copy(
//                graph = nextDungeon.graph.copy(
//                    outputTypes = nextDungeon.graph.outputTypes
//                        .plus(id to outputType)
//
//                )
//            )
//          else
            nextDungeon,
            matchingParenthesesErrors.plus(expressionErrors)
        )
      }
    }

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

fun finalizeDungeons(context: Context, nodeRanges: Map<PathKey, TokenizedDefinition>): (List<Dungeon>) -> PartitionedResponse<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.range }

      val initialGraph = newNamespace().copy(
//          nodes = nodeRanges.keys,
          connections = setOf(),
          values = mapOf()
      )

      val initialDungeon = Dungeon(
          graph = initialGraph,
          nodeMap = nodeMap
      )

      val mergedDungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
        mergeDistinctDungeons(a, expressionDungeon)
      }
      val propagations = propagateLiteralTypeAliases(context, mergedDungeon.graph)
      val dungeon = mergedDungeon.copy(
          graph = mergedDungeon.graph.copy(
              nodeTypes = mergedDungeon.graph.nodeTypes + propagations
          )
      )
      val constraintErrors = validateTypeConstraints(dungeon.graph.values, context, propagations, dungeon.nodeMap)
      val typeNames = gatherTypeNames(context, dungeon.graph.nodeTypes)

      PartitionedResponse(
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
    nodeRanges: Map<PathKey, TokenizedDefinition>,
    rawImportedFunctions: List<Map<PathKey, TypeHash>>,
    parentContext: Context): Context {
  val importedFunctions = if (rawImportedFunctions.any())
    rawImportedFunctions.reduce { a, b -> a + b }
  else
    mapOf()

  return parentContext.plus(
      newNamespace().copy(
          nodeTypes = importedFunctions
      )
  )
}

fun addGraphToContext(graph: Graph, context: Context): Context =
    context
        .dropLast(1)
        .plus(context.last() + graph)

fun parseDefinitions(nodeRanges: Map<PathKey, TokenizedDefinition>, initialContext: Context): PartitionedResponse<List<Dungeon>> {
  val (dungeonResponses) = nodeRanges.entries
      .fold<Map.Entry<PathKey, TokenizedDefinition>, Pair<List<PartitionedResponse<Dungeon>>, Context>>(Pair(listOf(), initialContext)) { a, b ->
        val (responses, context) = a
        val response = parseDefinition(context)(b)
        val nextContext = addGraphToContext(response.value.graph, context)
        Pair(responses.plus(response), nextContext)
      }

  return flattenResponses(dungeonResponses)
}

fun parseDungeon(parentContext: Context): (TokenizedGraph) -> PartitionedResponse<Dungeon> =
    { (imports, definitions) ->
      val (rawImportedFunctions, importErrors) = flattenResponses(imports.map(parseImport(parentContext.first())))
      val nodeRanges = definitions.associateBy { PathKey(localPath, it.symbol.value) }
      val baseContext = listOf(
          newNamespace()
              .copy(
                  typings = parentContext.map { it.typings }.reduce(::mergeTypings)
              )
      )
      val context = newDefinitionContext(nodeRanges, rawImportedFunctions, baseContext)
      val (dungeons, definitionErrors) = parseDefinitions(nodeRanges, context)
      val (dungeon, dungeonErrors) = finalizeDungeons(context, nodeRanges)(dungeons)
      PartitionedResponse(
          dungeon,
          importErrors + definitionErrors + dungeonErrors
      )
    }
