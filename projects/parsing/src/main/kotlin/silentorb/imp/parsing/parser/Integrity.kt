package silentorb.imp.parsing.parser

import silentorb.imp.core.Graph
import silentorb.imp.core.getGraphOutputNodes
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

val getImportErrors = { import: TokenizedImport ->
  val path = import.path
  val shouldBeIdentifiers = (0 until path.size - 1 step 2).map { path[it] }
  val shouldBeDots = (1 until path.size - 1 step 2).map { path[it] }
  val last = path.last()
  val invalidTokens = shouldBeIdentifiers.filterNot { it.rune == Rune.identifier }
      .plus(shouldBeDots.filterNot { it.rune == Rune.dot })
      .plus(listOf(last).filterNot { it.rune == Rune.identifier || (it.rune == Rune.operator && it.value == "*") })

  invalidTokens.map(newParsingError(TextId.invalidToken))
}

val checkImportTokens = checkForErrors { imports: List<TokenizedImport> ->
  imports.flatMap(getImportErrors)
}

val checkDefinitionTokens = checkForErrors { definitions: List<TokenizedDefinition> ->
  val duplicateSymbols = definitions
      .groupBy { it.symbol.value }
      .filter { it.value.size > 1 }

  val duplicateSymbolErrors = duplicateSymbols.flatMap { (_, definitions) ->
    definitions.drop(1).map { definition ->
      newParsingError(TextId.duplicateSymbol, definition.symbol)
    }
  }
  duplicateSymbolErrors
}

fun checkForGraphErrors(nodeMap: NodeMap) = checkForErrors { graph: Graph ->
  val graphOutputs = getGraphOutputNodes(graph)
  listOfNotNull(
      errorIf(graphOutputs.none(), TextId.noGraphOutput, Range(newPosition()))
  )
      .plus(graphOutputs.drop(1).map {
        val token = nodeMap[it]!!
        newParsingError(TextId.multipleGraphOutputs, token)
      })
}

val checkMatchingParentheses = checkForErrors { tokens: Tokens ->
  val openCount = tokens.count { it.rune == Rune.parenthesesOpen }
  val closeCount = tokens.count { it.rune == Rune.parenthesesClose }
  if (openCount > closeCount)
    listOf(newParsingError(TextId.missingClosingParenthesis, range = Range(tokens.last().range.end)))
  else if (closeCount > openCount) {
    val range = Range(tokens.last { it.rune == Rune.parenthesesClose }.range.end)
    listOf(newParsingError(TextId.unexpectedCharacter, range = range))
  } else
    listOf()
}
