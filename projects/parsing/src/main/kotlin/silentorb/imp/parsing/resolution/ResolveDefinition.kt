package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.parser.DefinitionFirstPass

fun newParameterNamespace(context: Context, pathKey: PathKey, parameters: List<Parameter>): Namespace {
  val pathString = pathKeyToString(pathKey)
  val nodeTypes = parameters.associate { parameter ->
    Pair(PathKey(pathString, parameter.name), parameter.type)
  }
  return newNamespace()
      .copy(
          nodeTypes = nodeTypes,
          typings = newTypings()
              .copy(
                  typeNames = nodeTypes.values
                      .associateWith { getTypeNameOrUnknown(context, it) }
              )
      )
}

fun prepareDefinitionFunction(
    namespace: Namespace,
    parameters: List<Parameter>,
    output: PathKey,
    outputType: TypeHash,
    key: PathKey,
    dungeon: Dungeon
): Dungeon {
  val signature = Signature(
      parameters = parameters,
      output = outputType,
      isVariadic = false
  )
  val definitionType = signature.hashCode()
  val typings = namespace.typings.copy(
      signatures = namespace.typings.signatures + (signature.hashCode() to signature)
  )
  val implementation = namespace.copy(
      connections = namespace.connections + (Input(
          destination = key,
          parameter = defaultParameter
      ) to output),
      nodeTypes = namespace.nodeTypes + (key to outputType),
      typings = typings
  )
  return dungeon.copy(
      namespace = newNamespace().copy(
          nodeTypes = mapOf(key to definitionType),
          typings = typings,
          values =  dungeon.namespace.values + (key.copy(type = definitionType) to FunctionSource(key, implementation))
      )
  )
}

fun parseDefinitionSecondPass(namespaceContext: Context, largerContext: Context, definition: DefinitionFirstPass): Response<Dungeon> {
  val parameters = definition.tokenized.parameters.map { parameter ->
    val type = getReturnTypesByName(namespaceContext, parameter.type.value as String).values.firstOrNull()
        ?: unknownType.hash
    Parameter(parameter.name.value as String, type)
  }
  val parameterNamespace = if (parameters.any()) {
    newParameterNamespace(namespaceContext, definition.key, parameters)
  } else
    null

  val parameterErrors = parameters
      .filter { it.type == unknownType.hash }
      .map { parameter->
        val details = definition.tokenized.parameters.first { it.name.value == parameter.name }
        newParsingError(TextId.unknownFunction, details.type)
      }
  val localContext = namespaceContext + listOfNotNull(parameterNamespace)

  val (dungeon, expressionErrors) = if (definition.definitions.any())
    parseBlock(localContext, largerContext, definition)
  else
    resolveExpression(localContext, largerContext, definition.intermediate!!)

  val output = getGraphOutputNode(dungeon.namespace)

  val nextDungeon = if (output != null) {
    val outputType = dungeon.namespace.nodeTypes[output]!!
    if (parameters.any()) {
      prepareDefinitionFunction(
          namespace = parameterNamespace!! + dungeon.namespace,
          parameters = parameters,
          output = output,
          outputType = outputType,
          key = definition.key,
          dungeon = dungeon
      )
    } else {
      val graph = dungeon.namespace
      dungeon.copy(
          namespace = graph.copy(
              connections = graph.connections + (Input(
                  destination = definition.key,
                  parameter = defaultParameter
              ) to output),
              nodeTypes = graph.nodeTypes + (definition.key to outputType)
          )
      )
    }
  } else
    dungeon

  return Response(
      nextDungeon,
      expressionErrors + parameterErrors
  )
}
