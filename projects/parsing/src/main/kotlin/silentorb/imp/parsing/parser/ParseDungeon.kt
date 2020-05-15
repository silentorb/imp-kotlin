package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.flattenResponses
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
      val matchingParenthesesErrors = checkMatchingParentheses(tokens)
      val path = PathKey(localPath, definition.symbol.value)
      val (dungeon, expressionErrors) = parseExpression(path, context, tokens)
      val output = getGraphOutputNode(dungeon.graph)
      val nextDungeon = addConnection(dungeon, Connection(
          source = output,
          destination = id,
          parameter = defaultParameter
      ))
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

fun finalizeDungeons(context: Context, nodeRanges: Map<PathKey, TokenizedDefinition>): (List<Dungeon>) -> PartitionedResponse<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.range }

      val initialGraph = Graph(
          nodes = nodeRanges.keys,
          connections = setOf(),
          values = mapOf()
      )

      val initialDungeon = Dungeon(
          graph = initialGraph,
          nodeMap = nodeMap,
          literalConstraints = mapOf()
      )

      val mergedDungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
        mergeDistinctDungeons(a, expressionDungeon)
      }
      val constraints = propagateTypeConstraints(mergeNamespaces(context), mergedDungeon.graph)
      val dungeon = mergedDungeon.copy(
          literalConstraints = constraints
      )
      val namespace = mergeNamespaces(context)
      val constraintErrors = validateTypeConstraints(dungeon.graph.values, namespace, constraints, dungeon.nodeMap)

      PartitionedResponse(
          dungeon.copy(
              graph = dungeon.graph.copy(
                  references = dungeon.graph.references.mapValues { resolveAlias(context, it.value) }
              )
          ),
          constraintErrors
      )
    }

fun newDefinitionContext(
    nodeRanges: Map<PathKey, TokenizedDefinition>,
    rawImportedFunctions: List<List<Pair<Key, PathKey>>>,
    parentContext: Context): Context {
  val importedFunctions = rawImportedFunctions
      .flatten()
      .associate { it }

  return parentContext.plus(
      newNamespace().copy(
          nodes = nodeRanges.keys,
          localFunctionAliases = importedFunctions
      )
  )
}

fun addGraphToContext(graph: Graph, context: Context): Context =
    context
        .dropLast(1)
        .plus(context.last().copy(
            references = context.last().references + graph.references
        ))

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

      val context = newDefinitionContext(nodeRanges, rawImportedFunctions, parentContext)
      val (dungeons, definitionErrors) = parseDefinitions(nodeRanges, context)
      val (dungeon, dungeonErrors) = finalizeDungeons(context, nodeRanges)(dungeons)
      PartitionedResponse(
          dungeon,
          importErrors.plus(definitionErrors).plus(dungeonErrors)
      )
    }
