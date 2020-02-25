package silentorb.imp.parsing.parser

import silentorb.imp.core.Id
import silentorb.imp.core.NextId
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

data class TokenOrGroup(
    val token: Int? = null,
    val group: Id? = null
)

data class TokenGroup(
    val depth: Int,
    val children: List<TokenOrGroup>
)

typealias TokenGroupsWithDepth = Map<Id, TokenGroup>

typealias TokenGroups = Map<Id, List<TokenOrGroup>>

data class ExpressionGraph(
    val groups: TokenGroups,
    val stages: List<List<Id>>
)

tailrec fun groupTokens(
    nextId: NextId,
    tokenIndex: Int,
    tokens: Tokens,
    groups: TokenGroupsWithDepth,
    groupStack: List<List<TokenOrGroup>>,
    depth: Int): TokenGroupsWithDepth {
  val token = tokens.first()
  val nextTokens = tokens.drop(1)
  val (nextStack, nextGroups, nextDepth) = when (token.rune) {
    Rune.parenthesesOpen -> Triple(groupStack.plusElement(listOf()), groups, depth + 1)
    Rune.parenthesesClose -> {
      val lastTwo = groupStack.takeLast(2)
      val id = nextId()
      val children = lastTwo.last()
      val nextGroups = groups.plus(id to TokenGroup(depth, children))
      val flattenedLayer = if (children.size > 1)
        lastTwo.first().plus(TokenOrGroup(group = id))
      else
        lastTwo.first()
      Triple(groupStack.dropLast(2).plusElement(flattenedLayer), nextGroups, depth - 1)
    }
    else -> {
      val appendedLayer = groupStack.last().plus(TokenOrGroup(token = tokenIndex))
      Triple(groupStack.dropLast(1).plusElement(appendedLayer), groups, depth)
    }
  }
  return if (nextTokens.none())
    if (nextStack.any() && nextStack.last().any())
      nextGroups.plus(nextId() to TokenGroup(depth, nextStack.last()))
    else
      nextGroups
  else {
    assert(nextStack.any())
    assert(nextTokens.any())
    groupTokens(nextId, tokenIndex + 1, nextTokens, nextGroups, nextStack, nextDepth)
  }
}

fun groupTokens(nextId: NextId, tokens: Tokens) =
    groupTokens(nextId, 0, tokens, mapOf(), listOf(listOf()), 1)

fun newExpressionGraph(groups: TokenGroupsWithDepth): ExpressionGraph =
    ExpressionGraph(
        groups = groups.mapValues { it.value.children },
        stages = groups.entries
            .groupBy { it.value.depth }
            .toList()
            .sortedByDescending { it.first }
            .map { it.second.map { it.key } }
    )
