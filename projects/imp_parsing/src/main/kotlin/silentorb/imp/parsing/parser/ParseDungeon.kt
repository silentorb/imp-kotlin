package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

data class TokenizedDefinition(
    val symbol: Token,
    val expression: Tokens
)

fun checkDungeonTokens(): (List<TokenizedDefinition>) -> Response<List<TokenizedDefinition>> = { definitions ->
  val duplicateSymbols = definitions
      .groupBy { it.symbol.value }
      .filter { it.value.size > 1 }

  val duplicateSymbolErrors = duplicateSymbols.flatMap { (_, definitions) ->
    definitions.drop(1).map { definition ->
      newParsingError(TextId.duplicateSymbol, definition.symbol)
    }
  }

  val errors = duplicateSymbolErrors

  if (errors.any())
    failure(errors)
  else
    success(definitions)
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

fun parseDungeon(context: Context): (List<TokenizedDefinition>) -> Response<Dungeon> = { definitions ->
  val passThroughNodes = definitions.mapIndexed { index, definition ->
    Pair(index.toLong(), definition)
  }
      .associate { it }

  data class ExpressionInfo(
      val owner: String,
      val token: Token
  )

  val expressionResults = definitions.mapIndexed { index, definition ->
    Pair((index + passThroughNodes.size).toLong(), ExpressionInfo(definition.symbol.value, parseExpression(context, definition.expression)))
  }
      .associate { it }

  val symbols = passThroughNodes.map { (id, definition) ->
    Pair(definition.symbol.value, id)
  }
      .associate { it }

  val values = expressionResults
      .filterValues { parseTokenValue(it.token) != null }
      .mapValues { (_, valueInfo) -> parseTokenValue(valueInfo.token)!! }

  val nodes: Set<Id> = passThroughNodes.keys
      .plus(values.keys)

  val nodeMap = passThroughNodes
      .mapValues { (_, definition) -> definition.symbol.range }
      .plus(expressionResults.mapValues { (_, valueInfo) -> valueInfo.token.range })

  val connections = expressionResults.map { (expressionId, value) ->
    val targetNode = symbols[value.owner]!!
    Connection(
        source = expressionId,
        destination = targetNode,
        parameter = defaultParameter
    )
  }
      .toSet()

  val graph = Graph(
      nodes = nodes,
      connections = connections,
      functions = mapOf(),
      values = values
  )

  checkForGraphErrors(nodeMap)(graph)
      .map { finalGraph ->
        Dungeon(
            graph = finalGraph,
            nodeMap = nodeMap
        )
      }
}
