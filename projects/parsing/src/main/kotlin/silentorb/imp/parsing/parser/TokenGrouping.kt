package silentorb.imp.parsing.parser

import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune


data class TokenGroup(
    val token: Token? = null,
    val children: List<TokenGroup> = listOf()
)

fun newTokenGroup(token: Token) =
    TokenGroup(token = token)

fun newTokenGroup(children: List<TokenGroup>): TokenGroup {
  assert(children.any())
  return TokenGroup(children = children)
}

typealias GroupedTokens = List<TokenGroup>

tailrec fun groupTokens(tokens: Tokens, groupStack: List<List<TokenGroup>>): Response<List<TokenGroup>> {
  val token = tokens.first()
  val nextTokens = tokens.drop(1)
  val nextStack = when (token.rune) {
    Rune.parenthesisOpen -> groupStack.plusElement(listOf())
    Rune.parenthesisClose -> {
      val lastTwo = groupStack.takeLast(2)
      groupStack.dropLast(2).plusElement(lastTwo.first().plus(newTokenGroup(lastTwo.last())))
    }
    else -> groupStack.dropLast(1).plusElement(groupStack.last().plus(newTokenGroup(token)))
  }
  return if (nextTokens.none()) {
    if (nextStack.size > 1)
      failure(ParsingError(TextId.missingClosingParenthesis, range = Range(token.range.end)))
    else
      success(nextStack.first())
  } else {
    assert(nextStack.any())
    assert(nextTokens.any())
    groupTokens(nextTokens, nextStack)
  }
}

fun groupTokens(tokens: Tokens) =
    groupTokens(tokens, listOf(listOf()))

tailrec fun getChildWithToken(group: TokenGroup): TokenGroup =
    if (group.token != null)
      group
    else
      getChildWithToken(group.children.first())
