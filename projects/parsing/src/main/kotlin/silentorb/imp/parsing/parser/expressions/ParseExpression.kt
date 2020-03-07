package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.core.NextId
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.failure
import silentorb.imp.parsing.general.success
import silentorb.imp.parsing.parser.Dungeon
import silentorb.imp.parsing.parser.validateFunctionTypes
import silentorb.imp.parsing.parser.validatePiping
import silentorb.imp.parsing.parser.validateSignatures

fun parseExpression(nextId: NextId, context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  val groupGraph = newGroupingGraph(groupTokens(tokens))
  val tokenGraph = arrangePiping(tokens, groupGraph)
  val namedArguments = tokenGraph.parents
      .map { (_, children) -> getNamedArguments(tokens, children) }
      .reduce { a, b -> a.plus(b) }
  val parents = collapseNamedArgumentClauses(namedArguments.keys, tokenGraph.parents)
  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val nodeReferences = resolveNodeReferences(context, tokens, indexedTokens)
  val tokenNodes = tokensToNodes(nextId, nodeReferences, indexedTokens)
//  val tokenNodes = nodeTokens.entries.associate { Pair(it.value, it.key) }
  val nodes = tokenNodes.values.toSet()
  val nonNodeReferenceTokens = indexedTokens.minus(nodeReferences.keys)
  val literalTypes = resolveLiteralTypes(tokens, nonNodeReferenceTokens, tokenNodes)
  val functionTypes = resolveFunctionTypes(context, tokens, nonNodeReferenceTokens, tokenNodes)
  val obviousTypes = literalTypes.plus(nodeReferences.values.associate { it })

  val (signatureOptions, functionReturnTypes) = resolveFunctionSignatures(
      context,
      tokenGraph,
      parents,
      functionTypes,
      obviousTypes,
      tokenNodes,
      tokens,
      namedArguments
  )
  val types = obviousTypes.plus(functionReturnTypes)
  val signatures = signatureOptions
      .filter { it.value.size == 1 }
      .mapValues { it.value.first() }
  val connections = arrangeConnections(parents, tokenNodes, signatures, namedArguments, types)
  val values = resolveLiterals(tokens, indexedTokens, tokenNodes)
  val newNodes = nodes.minus(nodeReferences.values.map { it.first })
  val nodeMap = newNodes
      .associateWith { id ->
        val index = tokenNodes.entries.first { it.value == id }.key
        tokens[index].range
      }
  val typeResolutionErrors = validateFunctionTypes(newNodes, functionTypes.plus(types), nodeMap)
  val signatureErrors = validateSignatures(signatureOptions, nodeMap)
  val pipingErrors = validatePiping(tokens, groupGraph)
  val errors = signatureErrors.plus(typeResolutionErrors).plus(pipingErrors)
  if (errors.any())
    failure(errors)
  else {
    assert(types.keys == nodes)
    success(Dungeon(
        graph = Graph(
            nodes = nodes,
            connections = connections,
            functionTypes = functionTypes,
            types = types,
            signatures = signatures,
            values = values
        ),
        nodeMap = nodeMap
    ))
  }
}
