package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.expressions.TokenGraph
import silentorb.imp.parsing.structureOld.getPipingParents
import java.nio.file.Path

val getImportErrors = { tokenizedImport: TokenizedImport ->
  val path = tokenizedImport.path
  val shouldBeIdentifiers = (0 until path.size - 1 step 2).map { path[it] }
  val shouldBeDots = (1 until path.size - 1 step 2).map { path[it] }
  if (path.none())
    listOf(newParsingError(TextId.missingImportPath, tokenizedImport.importToken))
  else {
    val last = path.last()
    val invalidTokens = shouldBeIdentifiers.filterNot { it.rune == Rune.identifier }
        .plus(shouldBeDots.filterNot { it.rune == Rune.dot })
        .plus(listOf(last).filterNot { it.rune == Rune.identifier || (it.rune == Rune.operator && it.value == "*") })

    invalidTokens.map(newParsingError(TextId.invalidToken))
  }
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
    listOf(newParsingError(TextId.missingClosingParenthesis, fileRange = FileRange(tokens.last().file, Range(tokens.last().range.end))))
  else if (closeCount > openCount) {
    val range = Range(tokens.last { it.rune == Rune.parenthesesClose }.range.end)
    listOf(newParsingError(TextId.unexpectedCharacter, FileRange(tokens.last().file, range)))
  } else
    listOf()
}

fun validateFunctionTypes(nodes: Set<PathKey>, types: Map<PathKey, TypeHash>, nodeMap: NodeMap): ParsingErrors {
  return nodes
      .filter { !types.containsKey(it) || types[it]!! == unknownType.hash }
      .map { node ->
        val fileRange = nodeMap[node]!!
        newParsingError(TextId.unknownFunction, fileRange)
      }
}

fun validateSignatures(context: Context, types: Map<PathKey, TypeHash>, parents: Map<PathKey, List<PathKey>>, signatureOptions: Map<PathKey, List<SignatureMatch>>,
                       nodeMap: NodeMap): ParsingErrors {
  return parents
      .mapNotNull { (pathKey, arguments) ->
        val options = signatureOptions[pathKey] ?: listOf()
        if (options.size == 1)
          null
        else if (options.none()) {
          val argumentTypeNames = arguments.map { argumentKey ->
            val argumentType = types[argumentKey]
            if (argumentType != null)
              getTypeNameOrUnknown(context, argumentType).name
            else
              unknownSymbol
          }

          val argumentClause = argumentTypeNames.joinToString(", ")
          ParsingError(TextId.noMatchingSignature, fileRange = nodeMap[pathKey]!!, arguments = listOf(argumentClause))
        } else
          ParsingError(TextId.ambiguousOverload, fileRange = nodeMap[pathKey]!!)
      }
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
    val groups = split(children, dividers).drop(1)
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

fun validateTypeConstraints(values: Map<PathKey, Any>, context: Context, constraints: ConstrainedLiteralMap, nodeMap: NodeMap): ParsingErrors {
  return values.mapNotNull { (node, value) ->
    val constraintType = constraints[node]
    if (constraintType != null) {
      val constraint = resolveNumericTypeConstraint(constraintType)(context)
      if (constraint == null || isValueWithinConstraint(constraint, value))
        null
      else
        newParsingError(TextId.outsideTypeRange, nodeMap[node]!!)
    } else null
  }
}

fun validateUnusedImports(context: Context, importMap: Map<Path, List<TokenizedImport>>, definitions: Map<PathKey, TokenizedDefinition>): ParsingErrors {
  val unusedImports = importMap
      .filter { (key, _) ->
        definitions.none { it.value.file == key }
      }

  return unusedImports.flatMap { (_, imports) ->
    imports
        .flatMap { tokenizedImport ->
          parseImport(context)(tokenizedImport).errors
        }
  }
}
