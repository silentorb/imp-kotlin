package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
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

fun definitionIndices(tokens: Tokens): List<Int> {
  val letTokens = tokens.indices.filter { index ->
    val token = tokens[index]
    token.rune == Rune.identifier && token.value == "let"
  }

  return letTokens.filter { index ->
    if (index < tokens.size - 2) {
      val symbol = tokens[index + 1]
      val equals = tokens[index + 2]
      symbol.rune == Rune.identifier
          && equals.rune == Rune.assignment
    } else
      false
  }
}

fun partitionDefinitions(tokens: Tokens): PartitionedResponse<List<DefinitionRange>> {
  val assignmentTokenIndices = definitionIndices(tokens)
  val entries = assignmentTokenIndices.map { step ->
    val token = tokens[step]
    val peek = peek(tokens, step)
    val neighbor = peek(-1)
    val symbol = peek(1)
    val firstExpressionToken = peek(3)
    fun formatError(condition: Boolean, textId: TextId, errorToken: Token?) =
        if (condition) null else newParsingError(textId, errorToken ?: token)

    val newErrors = listOfNotNull(
//        formatError(symbol?.rune == Rune.identifier, TextId.expectedIdentifier, symbol),
        formatError(neighbor?.rune == Rune.newline || neighbor?.rune == null, TextId.expectedNewline, neighbor),
        formatError(firstExpressionToken?.rune != Rune.newline && firstExpressionToken?.rune != null, TextId.expectedExpression, firstExpressionToken)
    )

    val expressionStart = step + 3
    val terminatorMatchIndex = nextIndexOf(tokens, expressionStart + 1) { it.rune == Rune.identifier && it.value == "let" }
    val expressionEnd = if (terminatorMatchIndex != null)
      terminatorMatchIndex
    else
      tokens.size

    assert(expressionStart < expressionEnd)
    val newDefinition = DefinitionRange(
        symbol = step + 1,
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
