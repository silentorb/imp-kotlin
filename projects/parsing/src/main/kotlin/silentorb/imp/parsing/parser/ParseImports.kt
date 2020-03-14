package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseImport(namespace: Namespace): (TokenizedImport) -> PartitionedResponse<List<Pair<Key, PathKey>>> = { import ->
  val path = import.path
      .filter { it.rune == Rune.identifier }
      .map { it.value }

  val hasWildcard = import.path.last().rune == Rune.operator

  if (hasWildcard) {
    val contents = getDirectoryContents(namespace, toPathString(path))
    if (contents.none()) {
      PartitionedResponse(listOf(), listOf(ParsingError(TextId.importNotFound, range = tokensToRange(import.path))))
    } else {
      PartitionedResponse(contents.map { Pair(it.name, it) }, listOf())
    }
  } else {
    val function = toPathKey(path)
    if (!namespace.functions.containsKey(function))
      PartitionedResponse(listOf(), listOf(ParsingError(TextId.importNotFound, range = tokensToRange(import.path))))
    else
      PartitionedResponse(listOf(Pair(function.name, function)), listOf())
  }
}
