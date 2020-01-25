package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

data class TokenizedDefinition(
    val symbol: Token,
    val expression: Tokens
)

fun checkDungeonTokens(): (List<TokenizedDefinition>) -> Response<List<TokenizedDefinition>> = { definitions ->
  val duplicateSymbols = definitions
      .groupBy { it.symbol.text }
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
    Pair((index + passThroughNodes.size).toLong(), ExpressionInfo(definition.symbol.text, parseExpression(context, definition.expression)))
  }
      .associate { it }

  val symbols = passThroughNodes.map { (id, definition) ->
    Pair(definition.symbol.text, id)
  }
      .associate { it }

//  val valueTokens = expressionResults.mapIndexed { index, definition ->
//    Pair((index + passThroughNodes.size).toLong(), definition)
//  }
//      .associate { it }

  val values = expressionResults.mapValues { (_, valueInfo) -> parseValueToken(valueInfo.token) }
  val valueMap = expressionResults.mapValues { (_, valueInfo) -> valueInfo.token.range }

  val nodes: Set<Id> = passThroughNodes.keys
      .plus(values.keys)

  val nodeMap = passThroughNodes
      .mapValues { (_, definition) -> definition.symbol.range }

  val connections = expressionResults.map { (expressionId, value) ->
    val targetNode = symbols[value.owner]!!
    Connection(
        source = expressionId,
        destination = targetNode,
        parameter = defaultParameter
    )
  }
      .toSet()

  val dungeon = Dungeon(
      graph = Graph(
          nodes = nodes,
          connections = connections,
          functions = mapOf(),
          values = values
      ),
      nodeMap = nodeMap,
      valueMap = valueMap
  )
  success(dungeon)
}
