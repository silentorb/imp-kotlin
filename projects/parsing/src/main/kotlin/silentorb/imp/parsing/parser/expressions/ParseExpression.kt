package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.core.NextId
import silentorb.imp.core.flattenAliases
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.*

fun parseExpression(nextId: NextId, context: Context, tokens: Tokens): PartitionedResponse<Dungeon> {
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

  val aliases = flattenAliases(context)
  val (signatureOptions, functionReturnTypes) = resolveFunctionSignatures(
      context,
      aliases,
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
  val connections = arrangeConnections(parents, tokenNodes, signatures)
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
  val dungeon = Dungeon(
      graph = Graph(
          nodes = nodes,
          connections = connections,
          functionTypes = functionTypes,
          types = types,
          signatureMatches = signatures,
          values = values
      ),
      nodeMap = nodeMap,
      literalConstraints = mapOf()
  )
  return PartitionedResponse(dungeon, errors)
}
