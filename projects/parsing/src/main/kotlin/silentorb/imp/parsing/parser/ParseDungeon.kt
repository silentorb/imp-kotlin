package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.*

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

fun parseDefinition(nextId: NextId, context: Context): (Map.Entry<Id, TokenizedDefinition>) -> PartitionedResponse<Dungeon> =
    { (id, definition) ->
      val tokens = definition.expression.filter { it.rune != Rune.newline }
      val matchingParenthesesErrors = checkMatchingParentheses(tokens)
      val (dungeon, expressionErrors) = parseExpression(nextId, context, tokens)
      val output = getGraphOutputNode(dungeon.graph)
      val nextDungeon = addConnection(dungeon, Connection(
          source = output,
          destination = id,
          parameter = defaultParameter
      ))
      val outputType = nextDungeon.graph.types[output]
      PartitionedResponse(
          if (outputType != null)
            nextDungeon.copy(
                graph = nextDungeon.graph.copy(
                    types = nextDungeon.graph.types
                        .plus(id to outputType)

                )
            )
          else
            nextDungeon,
          matchingParenthesesErrors.plus(expressionErrors)
      )
    }

fun finalizeDungeons(context: Context, nodeRanges: Map<Id, TokenizedDefinition>): (List<Dungeon>) -> PartitionedResponse<Dungeon> =
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
          dungeon,
          constraintErrors
      )
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
      newNamespace().copy(
          nodes = definitionSymbols,
          localFunctionAliases = importedFunctions
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

fun parseDefinitions(nextId: NextId, nodeRanges: Map<Id, TokenizedDefinition>, initialContext: Context): PartitionedResponse<List<Dungeon>> {
  val (dungeonResponses) = nodeRanges.entries
      .fold<Map.Entry<Id, TokenizedDefinition>, Pair<List<PartitionedResponse<Dungeon>>, Context>>(Pair(listOf(), initialContext)) { a, b ->
        val (responses, context) = a
        val response = parseDefinition(nextId, context)(b)
//        val (dungeon, errors) = response
        val nextContext = addTypesToContext(response.value.graph.types, context)
        Pair(responses.plus(response), nextContext)
      }

  return flattenResponses(dungeonResponses)
}

fun parseDungeon(parentContext: Context): (TokenizedGraph) -> PartitionedResponse<Dungeon> =
    { (imports, definitions) ->
      val (rawImportedFunctions, importErrors) = flattenResponses(imports.map(parseImport(parentContext.first())))
      val nextId = newIdSource(1L)
      val nodeRanges = definitions.associateBy { nextId() }

      val context = newDefinitionContext(nodeRanges, rawImportedFunctions, parentContext)
      val (dungeons, definitionErrors) = parseDefinitions(nextId, nodeRanges, context)
      val (dungeon, dungeonErrors) = finalizeDungeons(context, nodeRanges)(dungeons)
      PartitionedResponse(
          dungeon,
          importErrors.plus(definitionErrors).plus(dungeonErrors)
      )
    }
