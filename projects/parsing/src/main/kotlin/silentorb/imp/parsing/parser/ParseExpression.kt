package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun getNodeReference(context: Context): (Token) -> NodeReference? = { token ->
  if (token.rune == Rune.identifier)
    resolveNode(context, token.value)
  else
    null
}

typealias NodeReferenceMap = Map<Int, NodeReference>

fun getNodeReferences(context: Context): (Map<Int, Token>) -> NodeReferenceMap = { tokens ->
  tokens.mapNotNull { (index, token) ->
    val node = getNodeReference(context)(token)
    if (node != null)
      Pair(index, node)
    else
      null
  }
      .associate { it }
}

fun nodeReferenceToDungeon(token: Token, nodeReference: NodeReference): Dungeon {
  val (id, type) = nodeReference
  return Dungeon(
      graph = Graph(
          nodes = setOf(id),
          types = mapOf(id to type)
      ),
      nodeMap = mapOf(
          id to token.range
      )
  )
}

fun nodeReferencesToDungeons(tokens: Tokens, nodeReferences: NodeReferenceMap): List<Dungeon> {
  return nodeReferences.map { (index, nodeReference) ->
    val (id, type) = nodeReference
    val token = tokens[index]
    nodeReferenceToDungeon(token, nodeReference)
  }
}

val getLiteral: (Map.Entry<Int, Token>) -> Pair<Int, ResolvedLiteral>? = { (index, token) ->
  val literalValuePair = parseTokenLiteral(token)
  if (literalValuePair != null)
    Pair(index, literalValuePair)
  else
    null
}

fun getLiterals(tokens: Map<Int, Token>): Map<Int, ResolvedLiteral> {
  return tokens
      .mapNotNull(getLiteral)
      .associate { it }
}

fun getFunctionReference(context: Context): (Map.Entry<Int, Token>) -> Pair<Int, PathKey>? = { (index, token) ->
  if (token.rune == Rune.identifier || token.rune == Rune.operator) {
    val function = resolveFunction(context, token.value)
    if (function != null)
      Pair(index, function)
    else
      null
  } else
    null
}

fun getFunctionReferences(context: Context, tokens: Map<Int, Token>): Map<Int, PathKey> {
  return tokens
      .mapNotNull(getFunctionReference(context))
      .associate { it }
}

data class ExpressionResolution(
    val literals: Map<Int, ResolvedLiteral>,
    val nodeReferences: NodeReferenceMap,
    val functions: Map<Int, PathKey>
)

fun resolveExpressionTokens(context: Context, graph: ExpressionGraph, tokens: Tokens): ExpressionResolution {
  val tokenMap = tokens
      .mapIndexed { index, token -> Pair(index, token) }
      .associate { it }
  val literals = getLiterals(tokenMap)
  val secondTokens = tokenMap.minus(literals.keys)
  val nodeReferences = getNodeReferences(context)(secondTokens)
  val thirdTokens = tokenMap.minus(nodeReferences.keys)

  return ExpressionResolution(
      literals = literals,
      nodeReferences = nodeReferences,
      functions = getFunctionReferences(context, thirdTokens)
  )
}

fun literalToDungeon(id: Id, range: Range, literal: ResolvedLiteral): Dungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(
                id
            ),
            values = mapOf(
                id to literal.value
            ),
            types = mapOf(
                id to literal.type
            )
        ),
        nodeMap = mapOf(
            id to range
        )
    )

fun functionToDungeon(id: Id, range: Range, function: PathKey): Dungeon =
    Dungeon(
        graph = Graph(
            nodes = setOf(
                id
            ),
            types = mapOf(
                id to function
            )
        ),
        nodeMap = mapOf(
            id to range
        )
    )

fun expressionToDungeons(nextId: NextId, tokens: Tokens, expressionResolution: ExpressionResolution): Map<Int, Dungeon> {
  return tokens.mapIndexedNotNull { index, token ->
    val literal = expressionResolution.literals[index]
    val nodeReference = expressionResolution.nodeReferences[index]
    val function = expressionResolution.functions[index]
    val dungeon = when {
      function != null -> functionToDungeon(nextId(), token.range, function)
      literal != null -> literalToDungeon(nextId(), token.range, literal)
      nodeReference != null -> nodeReferenceToDungeon(token, nodeReference)
      else -> null
    }
    if (dungeon != null)
      Pair(index, dungeon)
    else
      null
  }
      .associate { it }
}

fun joinInvocationWithArguments(group: List<TokenOrGroup>): Dungeon {
  
}

fun expressionToDungeon(graph: ExpressionGraph,
                        dungeons: Map<Int, Dungeon>): Dungeon {
  val rootStage = graph.stages.last()
  assert(rootStage.size == 1)
  val rootGroup = graph.groups[rootStage.first()]!!
  return if (rootGroup.size == 1)
    dungeons[rootGroup.first().token!!]!!
  else
    joinInvocationWithArguments(rootGroup)
}

//fun parseExpressionToken(nextId: NextId): (Int) -> Dungeon = { tokenIndex ->
//
//  //  if (function == null && literalValuePair == null) {
////    failure(newParsingError(TextId.unknownFunction, token))
////  } else {
//  val id = nextId()
//
//  val nodes = setOf(id)
//  val values = if (literalValuePair != null) {
//    mapOf(
//        id to literalValuePair.second
//    )
//  } else
//    mapOf()
//
//  val nodeMap = mapOf(
//      id to token.range
//  )
//  val functions = if (function != null)
//    mapOf(
//        id to function
//    )
//  else
//    mapOf()
//
//  Dungeon(
//      graph = Graph(
//          nodes = nodes,
//          types = functions,
//          values = values
//      ),
//      nodeMap = nodeMap
//  )
////  }
//}

//fun parseNamedArgument(nextId: NextId, context: Context, tokens: GroupedTokens): Response<Pair<Token?, Dungeon>> {
//  return if (tokens.size == 1) {
//    failure(newParsingError(TextId.missingArgumentName, tokens[0].token!!))
//  } else if (tokens.size == 2) {
//    failure(newParsingError(TextId.missingArgumentExpression, tokens[1].token!!))
//  } else {
//    val symbolToken = getChildWithToken(tokens[1]).token!!
//    if (symbolToken.rune != Rune.identifier)
//      failure(newParsingError(TextId.expectedIdentifier, symbolToken))
//    else {
//      val first = parseExpression(nextId, context)(tokens[2])
//          .map { Pair<Token?, Dungeon>(symbolToken, it) }
//      if (tokens.size > 3) {
//        flatten(listOf(
//            first,
//            parseNamedArgumentOrExpression(nextId, context, tokens.drop(3))
//        ))
//      }
//      else
//        first
//    }
//  }
//}
//
//fun parseNamedArgumentOrExpression(nextId: NextId, context: Context, tokens: GroupedTokens): Response<List<Pair<Token?, Dungeon>>> {
//  val token = getChildWithToken(tokens.first()).token!!
//  return if (token.rune == Rune.operator && token.value == "<=")
//    parseNamedArgument(nextId, context, tokens)
//        .map { listOf(it) }
//  else
//    flatten(tokens.map {
//      parseExpression(nextId, context)(it)
//          .map { Pair<Token?, Dungeon>(null, it) }
//    })
//}
//
//fun parseArguments(nextId: NextId, context: Context, firstDungeon: Dungeon, tokens: GroupedTokens): Response<Dungeon> {
//  return parseNamedArgumentOrExpression(nextId, context, tokens)
//      .then { dungeonPairs ->
//        // Assuming that at least for now the first token only translates to a single node
//        val destination = firstDungeon.graph.nodes.first()
//        val arguments = dungeonPairs.map { (symbol, dungeon) ->
//          val output = getGraphOutputNode(dungeon.graph)
//          val type = dungeon.graph.types[output]
//              ?: throw Error("Graph is missing a type for node $output")
//          Argument(
//              name = symbol?.value,
//              type = type
//          )
//        }
//        val functionOverloads = getTypeDetails(context, firstDungeon.graph.types.values.first())!!
//        matchFunction(arguments, functionOverloads, tokensToRange(tokens.map { getChildWithToken(it).token!! }))
//            .map { signature ->
//              dungeonPairs.foldIndexed(firstDungeon) { i, a, (symbol, dungeon) ->
//                val source = getGraphOutputNode(dungeon.graph)
//                val parameter = symbol?.value ?: signature.parameters[i].name
//                mergeDistinctDungeons(a, dungeon)
//                    .addConnection(Connection(
//                        destination = destination,
//                        source = source,
//                        parameter = parameter
//                    ))
//              }
//                  .addSignature(destination, signature)
//            }
//      }
//}

//fun parseExpression(nextId: NextId, context: Context): (ExpressionGraph) -> Dungeon = { graph ->
//
//  //  val (token, children) = if (group.token != null)
////    Pair(group.token, listOf())
////  else
////    Pair(getChildWithToken(group).token!!, group.children.drop(1))
////
////  handle(parseExpressionToken(nextId, context)(token)) { firstDungeon ->
////    if (group.children.size > 1) {
////      parseArguments(nextId, context, firstDungeon, children)
////    } else
////      success(firstDungeon)
////  }
//  parseExpressionToken(nextId, context)(graph.groups[graph.stages.first().first()]!!.children.first().token!!)
//}
