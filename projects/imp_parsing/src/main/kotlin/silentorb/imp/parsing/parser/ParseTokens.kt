package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Graph
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

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
        formatError(neighbor?.rune == Rune.newline || neighbor?.rune == null, TextId.expectedNewline, neighbor),
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
  return if (tokens.none())
    success(listOf())
  else
    partitionDefinitions(tokens, listOf(), listOf(), 0)
}

fun parseTokens(context: Context): (Tokens) -> Response<Dungeon> = { tokens ->
  partitionDefinitions(tokens)
      .then(checkDungeonTokens)
      .then(parseDungeon(context))
}
