package silentorb.imp.parsing.parser

import silentorb.imp.core.Graph
import silentorb.imp.core.Node
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize

data class TokenizedDefinition(
    val symbol: Token,
    val expression: Tokens
)

fun peek(tokens: Tokens, position: Int): (Int) -> Token? = { offset ->
  val index = position + offset
  if (index > tokens.size - 1 || index < 0)
    null
  else
    tokens[index]
}

tailrec fun partitionDefinitions(
    tokens: Tokens,
    definitions: List<TokenizedDefinition>,
    errors: List<ParsingError>,
    step: Int
): Response<List<TokenizedDefinition>> {
  val token = tokens[step]
  val peek = peek(tokens, step)
  val isAssignment = token.rune == Rune.assignment
  val (nextErrors, nextDefinitions, nextStep) = if (isAssignment) {
    val neighbor = peek(-2)
    val symbol = peek(-1)
    val firstExpressionToken = peek(1)
    fun formatError(condition: Boolean, textId: TextId, errorToken: Token?) =
        if (condition) null else newParsingError(textId, errorToken ?: token)

    val newErrors = listOfNotNull(
        formatError(symbol?.rune == Rune.identifier, TextId.expectedIdentifier, symbol),
        formatError(neighbor?.rune == Rune.newline || neighbor?.rune == null, TextId.expectedExpression, neighbor),
        formatError(firstExpressionToken?.rune != Rune.newline && firstExpressionToken?.rune != null, TextId.expectedExpression, firstExpressionToken)
    )

    val farthestExpressionEnd = (step until tokens.size).firstOrNull { index ->
      val futureToken = tokens[index]
      futureToken.rune == Rune.identifier
    } ?: tokens.size - 1

    val expressionStart = step + 1
    val expressionEnd = (farthestExpressionEnd downTo step).firstOrNull { index ->
      val futureToken = tokens[index]
      futureToken.rune != Rune.newline
    } ?: farthestExpressionEnd

    val newDefinitions = if (newErrors.none()) {
      listOf(
          TokenizedDefinition(
              symbol = symbol!!,
              expression = tokens.slice(expressionStart..expressionEnd)
          )
      )
    } else
      listOf()

    Triple(errors.plus(newErrors), definitions.plus(newDefinitions), step + 1)
  } else
    Triple(errors, definitions, step + 1)


  return if (nextStep > tokens.size - 1) {
    if (errors.any())
      failure(errors)
    else
      success(definitions)
  } else
    partitionDefinitions(tokens, nextDefinitions, nextErrors, nextStep)
}

fun partitionDefinitions(tokens: Tokens): Response<List<TokenizedDefinition>> {
  return partitionDefinitions(tokens, listOf(), listOf(), 0)
}

fun parseTokens(context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  partitionDefinitions(tokens)
      .then { definitions ->
        val rawNodes = definitions.map { definition ->
          Triple(definition.symbol.text!!, definition.symbol, Node(
              type = ""
          ))
        }
        val values = definitions.associate { definition ->
          Pair(definition.symbol.text!!, definition.expression.first().text!!)
        }

        val nodes = rawNodes.associate { Pair(it.first, it.third) }
        val nodeMap = rawNodes.associate { Pair(it.first, it.second.range) }
        val dungeon = Dungeon(
            graph = Graph(
                nodes = nodes,
                connections = setOf(),
                values = mapOf()
            ),
            nodeMap = nodeMap,
            valueMap = mapOf()
        )
        success(dungeon)
      }
}

fun parseText(context: Context): (CodeBuffer) -> Response<Dungeon> = { code ->
  tokenize(code)
      .then(parseTokens(context))
}
