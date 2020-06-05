package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.DefinitionFirstPass
import silentorb.imp.parsing.parser.TokenizedDefinition
import silentorb.imp.parsing.parser.checkMatchingParentheses

fun parseDefinitionFirstPass(key: PathKey, definition: TokenizedDefinition): ParsingResponse<DefinitionFirstPass?> {
  val (intermediate, tokenErrors) = expressionTokensToNodes(key, definition.expression)
//  val matchingParenthesesErrors = checkMatchingParentheses(tokens)
  return ParsingResponse(
      DefinitionFirstPass(
          file = definition.file,
          key = key,
          tokenized = definition,
          intermediate = intermediate
      ),
      tokenErrors
  )
}
