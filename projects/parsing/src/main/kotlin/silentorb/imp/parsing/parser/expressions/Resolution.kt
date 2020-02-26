package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.ResolvedLiteral
import silentorb.imp.parsing.parser.parseTokenLiteral

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

//val getLiteral: (Map.Entry<Int, Token>) -> Pair<Int, ResolvedLiteral>? = { (index, token) ->
//  val literalValuePair = parseTokenLiteral(token)
//  if (literalValuePair != null)
//    Pair(index, literalValuePair)
//  else
//    null
//}

//fun getLiterals(tokens: Map<Int, Token>): Map<Int, ResolvedLiteral> {
//  return tokens
//      .mapNotNull(getLiteral)
//      .associate { it }
//}

fun getFunctionReference(context: Context): (Token) -> PathKey? = { token ->
  if (token.rune == Rune.identifier || token.rune == Rune.operator)
    resolveFunction(context, token.value)
  else
    null
}

//fun getFunctionReferences(context: Context, tokens: Map<Int, Token>): Map<Int, PathKey> {
//  return tokens
//      .mapNotNull(getFunctionReference(context))
//      .associate { it }
//}

data class ExpressionResolution(
    val literals: Map<Int, ResolvedLiteral>,
    val nodeReferences: NodeReferenceMap,
    val functions: Map<Int, PathKey>
)

//fun resolveExpressionTokens(context: Context, graph: TokenGraph, tokens: Tokens): ExpressionResolution {
//  val tokenMap = tokens
//      .mapIndexed { index, token -> Pair(index, token) }
//      .associate { it }
//  val literals = getLiterals(tokenMap)
//  val secondTokens = tokenMap.minus(literals.keys)
//  val nodeReferences = getNodeReferences(context)(secondTokens)
//  val thirdTokens = tokenMap.minus(nodeReferences.keys)
//
//  return ExpressionResolution(
//      literals = literals,
//      nodeReferences = nodeReferences,
//      functions = getFunctionReferences(context, thirdTokens)
//  )
//}

fun resolveNodeReferences(context: Context, tokens: Tokens, indexes: List<TokenIndex>): NodeReferenceMap {
  return indexes.mapNotNull {
    val reference = getNodeReference(context)(tokens[it])
    if (reference != null)
      Pair(it, reference)
    else
      null
  }
      .associate { it }
}

fun resolveLiterals(tokens: Tokens, indexes: List<TokenIndex>, tokenNodes: Map<TokenIndex, Id>): Map<Id, Any> {
  return indexes
      .mapNotNull { tokenIndex ->
        val literalValuePair = parseTokenLiteral(tokens[tokenIndex])
        if (literalValuePair != null)
          Pair(tokenNodes[tokenIndex]!!, literalValuePair)
        else
          null
      }
      .associate { it }
}
