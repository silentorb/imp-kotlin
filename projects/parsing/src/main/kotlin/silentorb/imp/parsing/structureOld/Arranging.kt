package silentorb.imp.parsing.structureOld

import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.TokenGraph
import silentorb.imp.parsing.parser.expressions.TokenIndex
import silentorb.imp.parsing.parser.expressions.TokenParents
import silentorb.imp.parsing.parser.filterIndexes
import silentorb.imp.parsing.parser.split

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

fun getPipingParents(tokens: Tokens, tokenGraph: TokenGraph): TokenParents {
  val pipeOperators = filterIndexes(tokens) { it.rune == Rune.dot }
  return tokenGraph.parents
      .filter { (_, children) ->
        children.any { pipeOperators.contains(it) }
      }
}

fun arrangePiping(tokens: Tokens, tokenGraph: TokenGraph): TokenGraph {
  val pipingParents = getPipingParents(tokens, tokenGraph)

  return if (pipingParents.none())
    tokenGraph
  else {
    val newParentPairs = pipingParents
        .mapValues { (_, children) ->
          split(children) { tokens[it].rune == Rune.dot }
              .drop(1)
              .filter { it.any() } // Filter out any bad groups.  Bad groups are handled separately by integrity checks.
        }

    val newParents = newParentPairs
        .flatMap { (parent, groups) ->
          groups
              .mapIndexed { index, indices ->
                val previous = groups.getOrNull(index - 1)?.first() ?: parent
                Pair(indices.first(), listOf(previous).plus(indices.drop(1)))
              }
        }
        .associate { it }

    val truncatedParents: TokenParents = pipingParents
        .mapValues { (_, children) ->
          val firstPipeToken = children.indexOfFirst { tokens[it].rune == Rune.dot }
          children.take(firstPipeToken)
        }

    val withReplacedChildren = tokenGraph.parents
        .plus(truncatedParents)
        .mapValues { (_, children) ->
          children
              .mapNotNull { child ->
                if (pipingParents.containsKey(child))
                  newParents.entries
                      .firstOrNull { it.value.contains(child) }
                      ?.key
                else
                  child
              }
        }

    assert(withReplacedChildren.keys.none { newParents.containsKey(it) })
    val parents = withReplacedChildren + newParents

    val stages = tokenGraph.stages
        .flatMap { stage ->
          val (needsExpanding, unchanged) = stage.partition {
            pipingParents.containsKey(it)
          }
          val expanded = needsExpanding.flatMap { parent ->
            listOf(listOf(parent)).plus(newParentPairs[parent]!!.map { it.take(1) })
          }
          if (unchanged.any())
            expanded.plus(listOf(unchanged))
          else
            expanded
        }

    TokenGraph(
        parents = parents,
        stages = stages
    )
  }
}
