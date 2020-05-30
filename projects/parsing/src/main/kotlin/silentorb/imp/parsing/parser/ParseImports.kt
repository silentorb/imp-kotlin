package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune

data class ImportBundle(
    val implementationTypes: Map<PathKey, TypeHash>,
    val returnTypes: Map<PathKey, TypeHash>
)

fun emptyImportBundle() =
    ImportBundle(
        implementationTypes = mapOf(),
        returnTypes = mapOf()
    )

fun parseImport(context: Context): (TokenizedImport) -> ParsingResponse<ImportBundle> = { import ->
  val path = import.path
      .filter { it.rune == Rune.identifier }
      .map { it.value }

  val hasWildcard = import.path.last().rune == Rune.operator

  if (hasWildcard) {
    val returnTypes = getNamespaceReturnTypes(context, toPathString(path))
    val implementationTypes = getNamespaceImplementationTypes(context, toPathString(path))

    if (returnTypes.none() && implementationTypes.none()) {
      ParsingResponse(emptyImportBundle(), listOf(ParsingError(TextId.importNotFound, tokensToFileRange(import.path))))
    } else {
      val bundle = ImportBundle(
          implementationTypes = implementationTypes,
          returnTypes = returnTypes
      )
      ParsingResponse(bundle, listOf())
    }
  } else {
    val pathKey = toPathKey(path)
    val returnType = getPathKeyTypes(context, pathKey).firstOrNull()
    val implementationType = getPathKeyImplementationTypes(context, pathKey).firstOrNull()
    if (returnType == null && implementationType == null)
      ParsingResponse(emptyImportBundle(), listOf(ParsingError(TextId.importNotFound, fileRange = tokensToFileRange(import.path))))
    else {
      val bundle = ImportBundle(
          implementationTypes = if (implementationType != null) mapOf(pathKey to implementationType) else mapOf(),
          returnTypes = if (returnType != null) mapOf(pathKey to returnType) else mapOf()
      )
      ParsingResponse(bundle, listOf())
    }
  }
}
