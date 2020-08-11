package silentorb.imp.parsing.resolution

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.parser.*
import java.nio.file.Path

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

fun parseBlock(context: Context, largerContext: Context, definition: DefinitionFirstPass): Response<Dungeon> {
  val definitions = definition.definitions
//      .associateBy { PathKey(formatPathKey(definition.key), it.tokenized.symbol.value as String) }

  val responses = resolveSubDefinitions(context, definitions, largerContext, listOf())
  val dungeons = mergeDungeons(responses.map { it.value })!!
  val errors = responses.flatMap { it.errors }
  return Response(dungeons, errors)
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
