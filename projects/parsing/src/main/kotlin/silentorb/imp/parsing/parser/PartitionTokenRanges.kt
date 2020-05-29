package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.core.Dungeon
import silentorb.imp.core.emptyDungeon
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun peek(tokens: Tokens, position: Int): (Int) -> Token? = { offset ->
  tokens.getOrNull(position + offset)
}

data class ImportRange(
    val start: Int,
    val end: Int
)

data class DefinitionRange(
    val symbol: Int,
    val parameters: List<TokenizedParameter>,
    val expressionStart: Int,
    val expressionEnd: Int
)

fun extractImportTokens(tokens: Tokens): (ImportRange) -> TokenizedImport = { range ->
  TokenizedImport(
      path = tokens.subList(range.start + 1, range.end) // Skip the "import" token
  )
}

fun extractDefinitionTokens(tokens: Tokens): (DefinitionRange) -> TokenizedDefinition = { range ->
  TokenizedDefinition(
      symbol = tokens[range.symbol],
      parameters = range.parameters,
      expression = tokens.subList(range.expressionStart, range.expressionEnd)
  )
}

typealias TokenFilter = (Token) -> Boolean

val isImportToken: TokenFilter = { token ->
  token.rune == Rune.identifier && token.value == "import"
}

val isAssignmentToken: TokenFilter = { token ->
  token.rune == Rune.assignment
}

val isImportTerminator: TokenFilter = { token ->
  token.rune == Rune.newline
}

val isDefinitionTerminator: TokenFilter = { token ->
  token.rune == Rune.newline
}

fun partitionImports(tokens: Tokens): List<ImportRange> {
  val importTokenIndices = filterIndices(tokens, isImportToken)
  return importTokenIndices.map { importTokenIndex ->
    val end = nextIndexOf(tokens, importTokenIndex, isImportTerminator) ?: tokens.size
    ImportRange(
        start = importTokenIndex,
        end = end
    )
  }
}

tailrec fun definitionAssignmentIndex(tokens: Tokens, step: Int): Int? {
  val current = tokens[step]
  return if (current.rune == Rune.assignment)
    step
  else if (current.rune != Rune.identifier && current.rune != Rune.colon)
    null
  else
    definitionAssignmentIndex(tokens, step + 1)
}

data class DefinitionIndex(
    val start: Int,
    val assigmentIndex: Int
)

fun definitionIndices(tokens: Tokens): List<DefinitionIndex> {
  val letTokens = tokens.indices.filter { index ->
    val token = tokens[index]
    token.rune == Rune.identifier && token.value == "let"
  }

  return letTokens.mapNotNull { index ->
    if (index < tokens.size - 2 && tokens[index + 1].rune == Rune.identifier) {
      val assigmentIndex = definitionAssignmentIndex(tokens, index + 2)
      if (assigmentIndex != null)
        DefinitionIndex(index, assigmentIndex)
      else
        null
    } else
      null
  }
}

tailrec fun partitionParameters(tokens: Tokens, start: Int, end: Int, accumulator: List<TokenizedParameter>): PartitionedResponse<List<TokenizedParameter>> {
  val length = end - start
  return if (length == 0)
    PartitionedResponse(accumulator, listOf())
  else if (length < 3) {
    PartitionedResponse(accumulator, listOf(newParsingError(TextId.incompleteParameter, tokens[start])))
  } else {
    val symbol = tokens[start]
    val separator = tokens[start + 1]
    val type = tokens[start + 2]
    if (symbol.rune != Rune.identifier)
      PartitionedResponse(accumulator, listOf(newParsingError(TextId.expectedIdentifier, symbol)))
    else if (separator.rune != Rune.colon)
      PartitionedResponse(accumulator, listOf(newParsingError(TextId.invalidToken, separator)))
    else if (type.rune != Rune.identifier)
      PartitionedResponse(accumulator, listOf(newParsingError(TextId.expectedIdentifier, type)))
    else {
      val parameter = TokenizedParameter(symbol.value, type.value)
      partitionParameters(tokens, start + 3, end, accumulator + parameter)
    }
  }
}

fun partitionDefinitions(tokens: Tokens): PartitionedResponse<List<DefinitionRange>> {
  val assignmentTokenIndices = definitionIndices(tokens)
  val entries = assignmentTokenIndices.map { (step, assigmentIndex) ->
    val token = tokens[step]
    val peek = peek(tokens, step)
    val neighbor = peek(-1)
    val firstExpressionToken = peek(3)
    fun formatError(condition: Boolean, textId: TextId, errorToken: Token?) =
        if (condition) null else newParsingError(textId, errorToken ?: token)

    val (parameters, parameterErrors) = partitionParameters(tokens, step + 2, assigmentIndex, listOf())

    val newErrors = listOfNotNull(
        formatError(neighbor?.rune == Rune.newline || neighbor?.rune == null, TextId.expectedNewline, neighbor),
        formatError(firstExpressionToken?.rune != Rune.newline && firstExpressionToken?.rune != null, TextId.expectedExpression, firstExpressionToken)
    ) + parameterErrors

    val expressionStart = assigmentIndex + 1
    val terminatorMatchIndex = nextIndexOf(tokens, expressionStart + 1) { it.rune == Rune.identifier && it.value == "let" }
    val expressionEnd = terminatorMatchIndex ?: tokens.size

    assert(expressionStart <= expressionEnd)
    val newDefinition = DefinitionRange(
        symbol = step + 1,
        parameters = parameters,
        expressionStart = expressionStart,
        expressionEnd = expressionEnd
    )

    Pair(newErrors, newDefinition)
  }

  val errors = entries.flatMap { it.first }

  return PartitionedResponse(entries.map { it.second }, errors)
}

fun toTokenizedGraph(
    tokens: Tokens,
    importRanges: List<ImportRange>,
    definitionRanges: List<DefinitionRange>
): PartitionedResponse<TokenizedGraph> {
  val imports = importRanges.map(extractImportTokens(tokens))
  val definitions = definitionRanges.map(extractDefinitionTokens(tokens))
  val importErrors = validateImportTokens(imports)
  val definitionErrors = validateDefinitionTokens(definitions)
  return PartitionedResponse(
      TokenizedGraph(
          imports = imports,
          definitions = definitions
      ),
      importErrors.plus(definitionErrors)
  )
}

fun getDefinitionsRangeStart(tokens: Tokens, definitionRanges: List<DefinitionRange>): Int =
    definitionRanges.minBy { it.symbol }?.symbol?.minus(1) ?: tokens.size

fun withoutComments(tokens: Tokens): Tokens =
    tokens.filter { it.rune != Rune.comment }

fun parseTokens(context: Context, tokens: Tokens): PartitionedResponse<Dungeon> {
  val (definitionRanges, partitionErrors) = partitionDefinitions(tokens)
  val importRangeMax = getDefinitionsRangeStart(tokens, definitionRanges)
  val importRanges = partitionImports(tokens.take(importRangeMax))
  val (tokenedGraph, tokenGraphErrors) = toTokenizedGraph(tokens, importRanges, definitionRanges)
  val (dungeon, dungeonErrors) = parseDungeon(context)(tokenedGraph)
  return PartitionedResponse(
      dungeon,
      partitionErrors.plus(tokenGraphErrors).plus(dungeonErrors)
  )
}

fun parseTokens(context: Context): (Tokens) -> PartitionedResponse<Dungeon> = { tokens ->
  assert(context.any())
  if (tokens.none())
    PartitionedResponse(emptyDungeon, listOf())
  else
    parseTokens(context, withoutComments(tokens))
}
