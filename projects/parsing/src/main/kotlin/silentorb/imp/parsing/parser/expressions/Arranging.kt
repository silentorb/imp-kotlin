package silentorb.imp.parsing.parser.expressions

import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune

fun getNamedArguments(tokens: Tokens, indices: List<TokenIndex>): Map<Int, String> {
  return (2 until indices.size).mapNotNull { index ->
    val parameterName = tokens[indices[index - 2]]
    val equals = tokens[indices[index - 1]]
    if (equals.rune == Rune.assignment && parameterName.rune == Rune.identifier)
      Pair(indices[index], parameterName.value)
    else
      null
  }
      .associate { it }
}

tailrec fun <T> nextIndex(list: List<T>, offset: Int, predicate: (T) -> Boolean): Int {
  return if (offset >= list.size)
    -1
  else if (predicate(list[offset]))
    offset
  else
    nextIndex(list, offset + 1, predicate)
}

tailrec fun collapseNamedArgumentClauses(namedArguments: Set<Int>, tokens: List<TokenIndex>, offset: Int): List<TokenIndex> {
  val match = nextIndex(tokens, offset) { namedArguments.contains(it) }
  return if (match == -1)
    tokens
  else {
    val nextTokens = tokens.take(match - 2).plus(tokens.drop(match))
    collapseNamedArgumentClauses(namedArguments, nextTokens, match)
  }
}

fun collapseNamedArgumentClauses(namedArguments: Set<Int>, parents: TokenParents): TokenParents {
  return if (namedArguments.none())
    parents
  else {
    parents.mapValues { (_, children) ->
      collapseNamedArgumentClauses(namedArguments, children, 2)
    }
  }
}
