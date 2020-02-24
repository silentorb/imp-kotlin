package silentorb.imp.parsing.parser

import silentorb.imp.core.Id
import silentorb.imp.core.NextId
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

data class TokenOrGroup(
    val token: Token? = null,
    val group: Id? = null
)

data class TokenGroup(
    val depth: Int,
    val children: List<TokenOrGroup>
)

typealias TokenGroups = Map<Id, TokenGroup>

data class ExpressionGraph(
    val groups: TokenGroups,
    val stages: List<List<Id>>
)

tailrec fun groupTokens(
    nextId: NextId,
    tokens: Tokens,
    groups: TokenGroups,
    groupStack: List<List<TokenOrGroup>>,
    depth: Int): TokenGroups {
  val token = tokens.first()
  val nextTokens = tokens.drop(1)
  val (nextStack, nextGroups, nextDepth) = when (token.rune) {
    Rune.parenthesesOpen -> Triple(groupStack.plusElement(listOf()), groups, depth + 1)
    Rune.parenthesesClose -> {
      val lastTwo = groupStack.takeLast(2)
      val id = nextId()
      val children = lastTwo.last()
      val nextGroups = groups.plus(id to TokenGroup(depth, children))
      val flattenedLayer = lastTwo.first().plus(TokenOrGroup(group = id))
      Triple(groupStack.dropLast(2).plusElement(flattenedLayer), nextGroups, depth - 1)
    }
    else -> {
      val appendedLayer = groupStack.last().plus(TokenOrGroup(token = token))
      Triple(groupStack.dropLast(1).plusElement(appendedLayer), groups, depth)
    }
  }
  return if (nextTokens.none())
    groups
  else {
    assert(nextStack.any())
    assert(nextTokens.any())
    groupTokens(nextId, nextTokens, groups, nextStack, nextDepth)
  }
}

fun groupTokens(nextId: NextId, tokens: Tokens) =
    groupTokens(nextId, tokens, mapOf(), listOf(listOf()), 1)

fun newExpressionGraph(groups: TokenGroups): ExpressionGraph =
    ExpressionGraph(
        groups = groups,
        stages = groups.entries
            .groupBy { it.value.depth }
            .toList()
            .sortedByDescending { it.first }
            .map { it.second.map { it.key } }
    )
