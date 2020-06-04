package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.BurgType

data class ImportBundle(
    val implementationTypes: Map<PathKey, TypeHash>,
    val returnTypes: Map<PathKey, TypeHash>
)

fun emptyImportBundle() =
    ImportBundle(
        implementationTypes = mapOf(),
        returnTypes = mapOf()
    )

fun parseImport(context: Context): (TokenizedImport) -> ParsingResponse<ImportBundle> = { tokenizedImport ->
  if (tokenizedImport.path.none()) {
    ParsingResponse(emptyImportBundle(), listOf())
  } else {
    val path = tokenizedImport.path
        .filter { it.type == BurgType.importPathToken }
        .map { it.value as String }

    val hasWildcard = tokenizedImport.path.last().type == BurgType.importPathWildcard

    if (hasWildcard) {
      val returnTypes = getReturnTypes(context, toPathString(path))
      val implementationTypes = getImplementationTypes(context, toPathString(path))

      if (returnTypes.none() && implementationTypes.none()) {
        ParsingResponse(emptyImportBundle(), listOf(ParsingError(TextId.importNotFound, burgsToFileRange(tokenizedImport.path))))
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
        ParsingResponse(emptyImportBundle(), listOf(ParsingError(TextId.importNotFound, fileRange = burgsToFileRange(tokenizedImport.path))))
      else {
        val bundle = ImportBundle(
            implementationTypes = if (implementationType != null) mapOf(pathKey to implementationType) else mapOf(),
            returnTypes = if (returnType != null) mapOf(pathKey to returnType) else mapOf()
        )
        ParsingResponse(bundle, listOf())
      }
    }
  }
}
