package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseImport(context: Context): (TokenizedImport) -> PartitionedResponse<Map<PathKey, TypeHash>> = { import ->
  val path = import.path
      .filter { it.rune == Rune.identifier }
      .map { it.value }

  val hasWildcard = import.path.last().rune == Rune.operator

  if (hasWildcard) {
    val contents = getNamespaceContents(context, toPathString(path))
    if (contents.none()) {
      PartitionedResponse(mapOf(), listOf(ParsingError(TextId.importNotFound, range = tokensToRange(import.path))))
    } else {
      PartitionedResponse(contents, listOf())
    }
  } else {
    val pathKey = toPathKey(path)
    val type = getPathKeyTypes(context, pathKey).firstOrNull()
    if (type == null)
      PartitionedResponse(mapOf(), listOf(ParsingError(TextId.importNotFound, range = tokensToRange(import.path))))
    else
      PartitionedResponse(mapOf(pathKey to type), listOf())
  }
}
