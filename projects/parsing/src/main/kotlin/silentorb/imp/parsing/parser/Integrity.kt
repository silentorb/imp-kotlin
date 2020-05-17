package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.TokenGraph
import silentorb.imp.parsing.parser.expressions.getPipingParents

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

fun validateImportTokens(imports: List<TokenizedImport>) =
    imports.flatMap(getImportErrors)

fun validateDefinitionTokens(definitions: List<TokenizedDefinition>): ParsingErrors {
  val duplicateSymbols = definitions
      .groupBy { it.symbol.value }
      .filter { it.value.size > 1 }

  return duplicateSymbols.flatMap { (_, definitions) ->
    definitions.drop(1).map { definition ->
      newParsingError(TextId.duplicateSymbol, definition.symbol)
    }
  }
}

fun checkMatchingParentheses(tokens: Tokens): ParsingErrors {
  val openCount = tokens.count { it.rune == Rune.parenthesesOpen }
  val closeCount = tokens.count { it.rune == Rune.parenthesesClose }
  return if (openCount > closeCount)
    listOf(newParsingError(TextId.missingClosingParenthesis, range = Range(tokens.last().range.end)))
  else if (closeCount > openCount) {
    val range = Range(tokens.last { it.rune == Rune.parenthesesClose }.range.end)
    listOf(newParsingError(TextId.unexpectedCharacter, range = range))
  } else
    listOf()
}

fun validateFunctionTypes(nodes: Set<PathKey>, types: Map<PathKey, TypeHash>, nodeMap: NodeMap): ParsingErrors {
  return nodes
      .filter { !types.containsKey(it) }
      .map { node ->
        val range = nodeMap[node]!!
        newParsingError(TextId.unknownFunction, range)
      }
}

fun validateSignatures(parents: Set<PathKey>, signatureOptions: Map<PathKey, List<SignatureMatch>>,
                       nodeMap: NodeMap): ParsingErrors {
  return parents
      .mapNotNull { pathKey ->
        val options = signatureOptions[pathKey] ?: listOf()
        if (options.size == 1)
          null
        else if (options.none())
          ParsingError(TextId.noMatchingSignature, range = nodeMap[pathKey]!!)
        else
          ParsingError(TextId.ambiguousOverload, range = nodeMap[pathKey]!!)
      }
}

fun validateMissingSignatures(
    functionTypes: Map<PathKey, PathKey>,
    signatures: Map<PathKey, SignatureMatch>,
    nodeMap: NodeMap
): ParsingErrors =
    functionTypes
        .minus(signatures.keys)
        .map { (id, _) ->
          ParsingError(TextId.noMatchingSignature, range = nodeMap[id]!!)
        }

fun validatePiping(tokens: Tokens, tokenGraph: TokenGraph): ParsingErrors {
  val pipingParents = getPipingParents(tokens, tokenGraph)
  val pipeTokens = filterIndices(tokens) { it.rune == Rune.dot }
  val flattenedPipeParentChildren = pipingParents.flatMap { it.value }
  val prematurePipeErrors = pipeTokens
      .filter { !flattenedPipeParentChildren.contains(it) }
      .map {
        newParsingError(TextId.missingExpression, tokens[it])
      }
  val danglingErrors = pipingParents.flatMap { (_, children) ->
    val dividers = filterIndices(children) { tokens[it].rune == Rune.dot }
    val groups = split(children, dividers)
    groups
        .mapIndexedNotNull { index, group ->
          if (group.none()) {
            val divider = dividers.getOrElse(index - 1) { dividers.last() }
            newParsingError(TextId.missingExpression, tokens[divider])
          } else
            null
        }
  }

  return prematurePipeErrors.plus(danglingErrors)
}

fun isValueWithinConstraint(constraint: NumericTypeConstraint, value: Any): Boolean {
  val doubleValue = when (value) {
    is Double -> value
    is Int -> value.toDouble()
    is Float -> value.toDouble()
    else -> null
  }
  return if (doubleValue == null)
    false
  else
    doubleValue >= constraint.minimum && doubleValue <= constraint.maximum
}

fun validateTypeConstraints(values: Map<PathKey, Any>, namespace: Namespace, constraints: ConstrainedLiteralMap, nodeMap: NodeMap): ParsingErrors {
  return values.mapNotNull { (node, value) ->
    val constraintType = constraints[node]
    if (constraintType != null) {
      val constraint = namespace.numericTypeConstraints[constraintType]!!
      if (isValueWithinConstraint(constraint, value))
        null
      else
        newParsingError(TextId.outsideTypeRange, nodeMap[node]!!)
    } else null
  }
}

fun validateGraph(nodeMap: NodeMap, graph: Graph): ParsingErrors {
  val graphOutputs = getGraphOutputNodes(graph)
  return listOfNotNull(
      errorIf(graphOutputs.none(), TextId.noGraphOutput, Range(newPosition()))
  )
      .plus(graphOutputs.drop(1).map {
        val token = nodeMap[it]!!
        newParsingError(TextId.multipleGraphOutputs, token)
      })
}
