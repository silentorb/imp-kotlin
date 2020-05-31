package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.mapExpressionTokensToNodes
import silentorb.imp.parsing.parser.expressions.parseExpression

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

fun prepareDefinitionFunction(
    graph: Graph,
    parameters: List<Parameter>,
    output: PathKey,
    outputType: TypeHash,
    id: PathKey,
    pathKey: PathKey,
    dungeon: Dungeon
): Dungeon {
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
      returnTypes = graph.returnTypes + (pathKey to outputType),
      typings = typings
  )
  return dungeon.copy(
      graph = newNamespace().copy(
          returnTypes = mapOf(pathKey to definitionType),
          typings = typings
      ),
      implementationGraphs = mapOf(
          FunctionKey(pathKey, definitionType) to implementation
      )
  )
}

fun parseDefinitionFirstPass(id: PathKey, definition: TokenizedDefinition): ParsingResponse<DefinitionFirstPass?> {
  val pathKey = PathKey(id.path, definition.symbol.value)
  val tokens = definition.expression.filter { it.rune != Rune.newline }
  return if (tokens.none()) {
    ParsingResponse(
        null,
        listOf(newParsingError(TextId.missingExpression, definition.symbol))
    )
  } else {
    val (intermediate, tokenErrors) = mapExpressionTokensToNodes(pathKey, tokens)
    val matchingParenthesesErrors = checkMatchingParentheses(tokens)
    ParsingResponse(
        DefinitionFirstPass(
            key = id,
            tokenized = definition,
            intermediate = intermediate
        ),
        tokenErrors + matchingParenthesesErrors
    )
  }
}

fun parseDefinitionSecondPass(context: Context, definition: DefinitionFirstPass): ParsingResponse<Dungeon> {
  val pathKey = PathKey(definition.key.path, definition.tokenized.symbol.value)
  val parameters = definition.tokenized.parameters.map { parameter ->
    val type = getImplementationType(context, parameter.type)
        ?: unknownType.hash
    Parameter(parameter.name, type)
  }
  val parameterNamespace = if (parameters.any()) {
    newParameterNamespace(context, pathKey, parameters)
  } else
    null

  val localContext = context + listOfNotNull(parameterNamespace)

  val (dungeon, expressionErrors) = parseExpression(localContext, definition.intermediate)

  val output = getGraphOutputNode(dungeon.graph)

  val nextDungeon = if (output != null) {
    val outputType = dungeon.graph.returnTypes[output]!!
    if (parameters.any()) {
      prepareDefinitionFunction(
          graph = parameterNamespace!! + dungeon.graph,
          parameters = parameters,
          output = output,
          outputType = outputType,
          id = definition.key,
          pathKey = pathKey,
          dungeon = dungeon
      )
    } else {
      val graph = dungeon.graph
      dungeon.copy(
          graph = graph.copy(
              connections = graph.connections + (Input(
                  destination = definition.key,
                  parameter = defaultParameter
              ) to output),
              returnTypes = graph.returnTypes + (pathKey to outputType)
          )
      )
    }
  } else
    dungeon

  return ParsingResponse(
      nextDungeon,
      expressionErrors
  )
}
