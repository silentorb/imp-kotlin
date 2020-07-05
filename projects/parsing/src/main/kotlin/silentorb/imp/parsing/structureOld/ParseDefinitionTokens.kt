package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.core.formatPathKey
import silentorb.imp.core.Response
import silentorb.imp.core.flattenResponses
import silentorb.imp.parsing.parser.DefinitionFirstPass
import silentorb.imp.parsing.parser.TokenizedDefinition

fun parseDefinitionFirstPass(key: PathKey, definition: TokenizedDefinition): Response<DefinitionFirstPass?> {
  val (intermediate, tokenErrors) = if (definition.expression != null)
    expressionTokensToNodes(key, definition.expression)
  else
    Response(null, listOf())

  val (definitions, subErrors) = flattenResponses(
      definition.definitions
          .map {
            val subKey = PathKey(formatPathKey(key), it.symbol.value.toString())
            parseDefinitionFirstPass(subKey, it)
          }
  )
  assert(definitions.filterNotNull().any() || intermediate != null)
  return Response(
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
