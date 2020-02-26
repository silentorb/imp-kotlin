package silentorb.imp.parsing.parser.expressions

import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

data class TokenGroup(
    val depth: Int,
    val children: List<TokenIndex>
)

typealias TokenGroups = Map<TokenIndex, TokenGroup>

fun accumulate(top: List<TokenIndex>, depth: Int, accumulator: TokenGroups): TokenGroups {
  return accumulator.plus(top.first() to TokenGroup(depth, top.drop(1)))
}

fun updateStack(rune: Rune, tokenIndex: TokenIndex, stack: List<List<TokenIndex>>) =
    when (rune) {
      Rune.parenthesesOpen -> stack.plusElement(listOf())
      Rune.parenthesesClose -> {
        val lastTwo = stack.takeLast(2)
        val returnValue = lastTwo.last().first()
        stack.dropLast(2).plusElement(lastTwo.first().plus(returnValue))
      }
      else -> {
        stack.dropLast(1).plusElement(stack.last().plus(tokenIndex))
      }
    }

tailrec fun groupTokens(
    tokenIndex: TokenIndex,
    tokens: Tokens,
    accumulator: TokenGroups,
    stack: List<List<TokenIndex>>
): TokenGroups {
  val token = tokens[tokenIndex]
  val nextStack = updateStack(token.rune, tokenIndex, stack)
  val isAtEnd = tokenIndex == tokens.size - 1
  val nextAccumulator = if (token.rune == Rune.parenthesesClose)
    accumulate(stack.last(), stack.size, accumulator)
  else
    accumulator

  return if (isAtEnd)
    accumulate(nextStack.last(), nextStack.size, nextAccumulator)
  else
    groupTokens(tokenIndex + 1, tokens, nextAccumulator, nextStack)
}

fun groupTokens(tokens: Tokens) =
    groupTokens(0, tokens, mapOf(), listOf(listOf()))

fun newGroupingGraph(groups: TokenGroups): TokenGraph =
    TokenGraph(
        parents = groups.mapValues { it.value.children },
        stages = groups.entries
            .groupBy { it.value.depth }
            .toList()
            .sortedByDescending { it.first }
            .map { it.second.map { it.key } }
    )
