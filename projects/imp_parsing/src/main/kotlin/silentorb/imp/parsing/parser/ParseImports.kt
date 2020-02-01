package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseImport(namespace: Namespace): (TokenizedImport) -> Response<List<Pair<Key, PathKey>>> = { import ->
  val path = import.path
      .filter { it.rune == Rune.identifier }
      .map { it.value }

  val hasWildcard = import.path.last().rune == Rune.operator

  if (hasWildcard) {
    val contents = getDirectoryContents(namespace, toPathString(path))
    if (contents.none()) {
      failure(ParsingError(TextId.importNotFound, range = tokensToRange(import.path)))
    } else {
      success(contents.map { Pair(it.name, it) })
    }
  } else {
    val function = toPathKey(path)
    if (!namespace.functions.containsKey(function))
      failure(ParsingError(TextId.importNotFound, range = tokensToRange(import.path)))
    else
      success(listOf(Pair(function.name, function)))
  }
}
