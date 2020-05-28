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

fun newParameterNamespace(context: Context, pathKey: PathKey, parameters: List<Parameter>): Namespace {
  val pathString = pathKeyToString(pathKey)
  val nodeTypes = parameters.associate { parameter ->
    Pair(PathKey(pathString, parameter.name), parameter.type)
  }
  return newNamespace()
      .copy(
          returnTypes = nodeTypes,
          typings = newTypings()
              .copy(
                  typeNames = nodeTypes.values
                      .associateWith { getTypeNameOrUnknown(context, it) }
              )
      )
}

fun parseDefinition(context: Context): (Map.Entry<PathKey, TokenizedDefinition>) -> PartitionedResponse<Dungeon> =
    { (id, definition) ->
      val tokens = definition.expression.filter { it.rune != Rune.newline }
      if (tokens.none()) {
        PartitionedResponse(
            emptyDungeon,
            listOf(newParsingError(TextId.missingExpression, definition.symbol))
        )
      } else {
        val matchingParenthesesErrors = checkMatchingParentheses(tokens)
        val pathKey = PathKey(localPath, definition.symbol.value)
        val parameters = definition.parameters.map { parameter ->
          val type = getImplementationType(context, parameter.type)
              ?: unknownType.hash
          Parameter(parameter.name, type)
        }
        val parameterNamespace = if (parameters.any()) {
          newParameterNamespace(context, pathKey, parameters)
        } else
          null

        val localContext = context + listOfNotNull(parameterNamespace)

        val (dungeon, expressionErrors) = parseExpression(pathKey, localContext, tokens)

        val output = getGraphOutputNode(dungeon.graph)
        val graph = if (parameterNamespace != null)
          parameterNamespace + dungeon.graph
        else
          dungeon.graph

        val nextDungeon = if (output != null) {
          val outputType = graph.returnTypes[output]!!
          if (parameters.any()) {
            val signature = Signature(
                parameters = parameters,
                output = outputType
            )
            val definitionType = signature.hashCode()
            val typings = graph.typings.copy(
                signatures = graph.typings.signatures + (signature.hashCode() to signature)
            )
            val implementation = graph.copy(
                connections = graph.connections + (Input(
                    destination = id,
                    parameter = defaultParameter
                ) to output),
                returnTypes = graph.returnTypes + (pathKey to definitionType),
                typings = typings
            )
            dungeon.copy(
                graph = newNamespace().copy(
                    returnTypes = mapOf(pathKey to definitionType),
                    typings = typings
                ),
                implementationGraphs = mapOf(
                    FunctionKey(pathKey, definitionType) to implementation
                )
            )
          } else {
            dungeon.copy(
                graph = graph.copy(
                    connections = graph.connections + (Input(
                        destination = id,
                        parameter = defaultParameter
                    ) to output),
                    returnTypes = graph.returnTypes + (pathKey to outputType)
                )
            )
          }
        } else
          dungeon

        PartitionedResponse(
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
      val (rawImportedFunctions, importErrors) = flattenResponses(imports.map(parseImport(parentContext)))
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
