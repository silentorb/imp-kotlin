package silentorb.imp.parsing.resolution

import silentorb.imp.core.Context
import silentorb.imp.core.PathKey
import silentorb.imp.core.resolveReference
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.parseTokenLiteral

//fun getNodeReference(context: Context): (Token) -> PathKey? = { token ->
//  if (token.rune == Rune.identifier || token.rune == Rune.operator)
//    resolveReference(context, token.value)
//  else
//    null
//}

//typealias NodeReferenceMap = Map<Int, PathKey>

//fun getNodeReferences(context: Context): (Map<Int, Token>) -> NodeReferenceMap = { tokens ->
//  tokens.mapNotNull { (index, token) ->
//    val node = getNodeReference(context)(token)
//    if (node != null)
//      Pair(index, node)
//    else
//      null
//  }
//      .associate { it }
//}
//
//fun resolveReferences(context: Context, tokens: Tokens, indexes: List<TokenIndex>): Map<Int, PathKey> {
//  return indexes.mapNotNull {
//    val reference = getNodeReference(context)(tokens[it])
//    if (reference != null)
//      Pair(it, reference)
//    else
//      null
//  }
//      .associate { it }
//}
//
//fun resolveLiterals(tokens: Tokens, indexes: List<TokenIndex>, tokenNodes: Map<TokenIndex, PathKey>): Map<PathKey, Any> {
//  return indexes
//      .mapNotNull { tokenIndex ->
//        val literalValuePair = parseTokenLiteral(tokens[tokenIndex])
//        if (literalValuePair != null)
//          Pair(tokenNodes[tokenIndex]!!, literalValuePair)
//        else
//          null
//      }
//      .associate { it }
//}
