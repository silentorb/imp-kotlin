package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.Dungeon

fun parseExpression(nextId: NextId, context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  val groupingGraph = newGroupingGraph(groupTokens(tokens))
  val namedArguments = groupingGraph.parents
      .map { (_, children) -> getNamedArguments(tokens, children) }
      .reduce { a, b -> a.plus(b) }
  val parents = collapseNamedArgumentClauses(namedArguments.keys, groupingGraph.parents)
  val indexedTokens = parents.keys.plus(parents.values.flatten()).toList()
  val nodeReferences = resolveNodeReferences(context, tokens, indexedTokens)
  val nodeTokens = tokensToNodes(nextId, nodeReferences, indexedTokens)
  val tokenNodes = nodeTokens.entries.associate { Pair(it.value, it.key) }
  val nodes = nodeTokens.keys
  val nonNodeReferenceTokens = indexedTokens.minus(nodeReferences.keys)
  val (localTypes, typeResolutionErrors) = resolveTypes(context, tokens, nonNodeReferenceTokens, tokenNodes)
  val types = localTypes.plus(nodeReferences.values.associate { it })
  val invocations = resolveInvocationArguments(parents, types, tokenNodes, tokens, namedArguments)
  val (signatures, signatureErrors) = resolveSignatures(context, invocations)
  val connections = arrangeConnections(parents, tokenNodes, signatures, namedArguments)
  val values = resolveLiterals(tokens, indexedTokens, tokenNodes)
  val errors = signatureErrors.plus(typeResolutionErrors)
  if (errors.any())
    failure(errors)
  else {
    assert(types.keys == nodes)
    success(Dungeon(
        graph = Graph(
            nodes = nodes,
            connections = connections,
            types = types,
            signatures = signatures,
            values = values
        ),
        nodeMap = nodeTokens.mapValues { tokens[it.value].range }
    ))
  }
}
