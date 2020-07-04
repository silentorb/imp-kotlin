package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.formatPathKey
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.flattenResponses
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.DefinitionFirstPass
import silentorb.imp.parsing.parser.TokenizedDefinition
import silentorb.imp.parsing.parser.checkMatchingParentheses

fun parseDefinitionFirstPass(key: PathKey, definition: TokenizedDefinition): ParsingResponse<DefinitionFirstPass?> {
  val (intermediate, tokenErrors) = if (definition.expression != null)
    expressionTokensToNodes(key, definition.expression)
  else
    ParsingResponse(null, listOf())

  val (definitions, subErrors) = flattenResponses(
      definition.definitions
          .map {
            val subKey = PathKey(formatPathKey(key), it.symbol.value.toString())
            parseDefinitionFirstPass(subKey, it)
          }
  )
  assert(definitions.filterNotNull().any() || intermediate != null)
  return ParsingResponse(
      DefinitionFirstPass(
          file = definition.file,
          key = key,
          tokenized = definition,
          intermediate = intermediate,
          definitions = definitions.filterNotNull()
      ),
      tokenErrors + subErrors
  )
}
