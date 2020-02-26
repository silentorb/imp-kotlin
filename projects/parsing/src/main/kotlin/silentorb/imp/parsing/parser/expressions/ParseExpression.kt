package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.parser.Dungeon

//fun joinInvocationWithArguments(group: List<TokenIndex>): Dungeon {
//  val destination = group.first()
//  val arguments = group.drop(1)
//      .map { (symbol, dungeon) ->
//    val output = getGraphOutputNode(dungeon.graph)
//    val type = dungeon.graph.types[output]
//        ?: throw Error("Graph is missing a type for node $output")
//    Argument(
//        name = symbol?.value,
//        type = type
//    )
//  }
//  val functionOverloads = getTypeDetails(context, firstDungeon.graph.types.values.first())!!
//  matchFunction(arguments, functionOverloads, tokensToRange(tokens.map { getChildWithToken(it).token!! }))
//      .map { signature ->
//        dungeonPairs.foldIndexed(firstDungeon) { i, a, (symbol, dungeon) ->
//          val source = getGraphOutputNode(dungeon.graph)
//          val parameter = symbol?.value ?: signature.parameters[i].name
//          mergeDistinctDungeons(a, dungeon)
//              .addConnection(Connection(
//                  destination = destination,
//                  source = source,
//                  parameter = parameter
//              ))
//        }
//            .addSignature(destination, signature)
//      }
//}

//fun expressionToDungeon(graph: TokenGraph,
//                        dungeons: Map<Int, Dungeon>): Dungeon {
//  val rootStage = graph.stages.last()
//  assert(rootStage.size == 1)
//  val rootGroup = graph.groups[rootStage.first()]!!
//  return if (rootGroup.size == 1)
//    dungeons[rootGroup.first()]!!
//  else
//    joinInvocationWithArguments(rootGroup)
//}

fun parseExpression(nextId: NextId, context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  val groupingGraph = newGroupingGraph(groupTokens(tokens))
  val namedArguments = getNamedArguments(tokens)
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

//  val expressionResolution = resolveExpressionTokens(context, groupingGraph, tokens)
//  val dungeons = expressionToDungeons(nextId, tokens, expressionResolution)

//  validateExpressionDungeons(groupingGraph, tokens)(dungeons)
//      .map {
//        expressionToDungeon(groupingGraph, dungeons)
//      }
}
