package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun getRuneType(rune: Rune): PathKey? =
    when (rune) {
      Rune.literalInteger -> intKey
      Rune.literalFloat -> floatKey
      else -> null
    }

fun resolveTypes(context: Context, tokens: Tokens, indexes: List<TokenIndex>,
                 tokenNodes: Map<TokenIndex, Id>): PartitionedResponse<Map<Id, PathKey>> {
  return partitionMap(
      indexes
          .associate { tokenIndex ->
            val token = tokens[tokenIndex]
            val type = getRuneType(token.rune) ?: getFunctionReference(context)(token)
            val id = tokenNodes[tokenIndex]!!
            val value = if (type != null)
              success(type)
            else
              failure(newParsingError(TextId.unknownFunction, token))

            Pair(id, value)
          }
  )
}
