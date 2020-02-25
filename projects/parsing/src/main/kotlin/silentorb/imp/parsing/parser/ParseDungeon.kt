package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.flatten

data class TokenizedImport(
    val path: Tokens
)

data class TokenizedDefinition(
    val symbol: Token,
    val expression: Tokens
)

data class TokenizedGraph(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

fun parseDefinition(nextId: NextId, context: Context): (Map.Entry<Id, TokenizedDefinition>) -> Response<Dungeon> =
    { (id, definition) ->
      checkMatchingParentheses(definition.expression)
          .then { tokens ->
            val expressionGraph = newExpressionGraph(groupTokens(newIdSource(1L), tokens))
            val expressionResolution = resolveExpressionTokens(context, expressionGraph, tokens)
            val dungeons = expressionToDungeons(nextId, tokens, expressionResolution)
            validateExpressionDungeons(expressionGraph, tokens)(dungeons)
                .map { Triple(tokens, expressionGraph, dungeons) }
          }
          .map { (tokens, expressionGraph, dungeons) ->
            val dungeon = expressionToDungeon(expressionGraph, dungeons)
            val output = getGraphOutputNode(dungeon.graph)
            val nextDungeon = addConnection(dungeon, Connection(
                source = output,
                destination = id,
                parameter = defaultParameter
            ))
            nextDungeon.copy(
                graph = nextDungeon.graph.copy(
                    types = nextDungeon.graph.types
                        .plus(id to nextDungeon.graph.types[output]!!)
                )
            )
          }
    }

fun finalizeDungeons(nodeRanges: Map<Id, TokenizedDefinition>): (List<Dungeon>) -> Response<Dungeon> =
    { expressionDungeons ->
      val nodeMap = nodeRanges
          .mapValues { (_, definition) -> definition.symbol.range }

      val initialGraph = Graph(
          nodes = nodeRanges.keys,
          connections = setOf(),
          types = mapOf(),
          values = mapOf()
      )

      val initialDungeon = Dungeon(
          graph = initialGraph,
          nodeMap = nodeMap
      )

      val dungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
        mergeDistinctDungeons(a, expressionDungeon)
      }

      checkForGraphErrors(dungeon.nodeMap)(dungeon.graph)
          .map { dungeon }
    }

fun newDefinitionContext(
    nodeRanges: Map<Id, TokenizedDefinition>,
    rawImportedFunctions: List<List<Pair<Key, PathKey>>>,
    parentContext: Context): Context {
  val definitionSymbols = nodeRanges.map { (id, definition) ->
    Pair(definition.symbol.value, id)
  }
      .associate { it }

  val importedFunctions = rawImportedFunctions
      .flatten()
      .associate { it }

  return parentContext.plus(
      Namespace(
          nodes = definitionSymbols,
          functionAliases = importedFunctions
      )
  )
}

fun addTypesToContext(types: Map<Id, PathKey>, context: Context): Context =
    context
        .dropLast(1)
        .plus(context.last().copy(
            types = context.last().types
                .plus(types)
        ))

fun parseDefinitions(nextId: NextId, nodeRanges: Map<Id, TokenizedDefinition>, initialContext: Context): Response<List<Dungeon>> {
  val (dungeonResponses) = nodeRanges.entries
      .fold<Map.Entry<Id, TokenizedDefinition>, Pair<List<Response<Dungeon>>, Context>>(Pair(listOf(), initialContext)) { a, b ->
        val (responses, context) = a
        val response = parseDefinition(nextId, context)(b)
        val nextContext = if (response is Response.Success)
          addTypesToContext(response.value.graph.types, context)
        else
          context

        Pair(responses.plus(response), nextContext)
      }

  return flatten(dungeonResponses)
}

fun parseDungeon(parentContext: Context): (TokenizedGraph) -> Response<Dungeon> =
    { (imports, definitions) ->
      flatten(imports.map(parseImport(parentContext.first())))
          .then { rawImportedFunctions ->
            val nextId = newIdSource(1L)
            val nodeRanges = definitions.mapIndexed { index, definition ->
              Pair(nextId(), definition)
            }
                .associate { it }

            val context = newDefinitionContext(nodeRanges, rawImportedFunctions, parentContext)
            parseDefinitions(nextId, nodeRanges, context)
                .then(finalizeDungeons(nodeRanges))
          }
    }
