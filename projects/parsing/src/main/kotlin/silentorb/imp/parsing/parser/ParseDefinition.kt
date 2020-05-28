package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
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
                returnTypes = graph.returnTypes + (pathKey to outputType),
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
