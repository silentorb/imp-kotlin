package silentorb.imp.parsing.parser

import silentorb.imp.core.Namespace
import silentorb.imp.core.resolveNamespaceFunctionPath
import silentorb.imp.core.resolveNamespacePath
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

fun parseImport(namespace: Namespace): (TokenizedImport) -> Response<List<Pair<String, String>>> = { import ->
  val path = import.path
      .filter { it.rune == Rune.identifier }
      .map { it.value }

  val hasWildcard = import.path.last().rune == Rune.operator

  if (hasWildcard) {
    val endNamespace = resolveNamespacePath(namespace, path)
    if (endNamespace == null) {
      failure(ParsingError(TextId.importNotFound, range = tokensToRange(import.path)))
    } else {
      success(endNamespace.functions.map { Pair(it.key, it.value) })
    }
  } else {
    val function = resolveNamespaceFunctionPath(namespace, path)
    if (function == null)
      failure(ParsingError(TextId.importNotFound, range = tokensToRange(import.path)))
    else
      success(listOf(
          Pair(import.path.last().value, function)
      ))
  }
}
