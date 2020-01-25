package silentorb.imp.parsing.parser

import silentorb.imp.core.Graph
import silentorb.imp.core.getGraphOutputNodes
import silentorb.imp.parsing.general.*

val checkDungeonTokens = checkForErrors { definitions: List<TokenizedDefinition> ->
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
