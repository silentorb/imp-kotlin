package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.*
import silentorb.imp.parsing.syntax.BurgType

data class ImportBundle(
    val returnTypes: Map<PathKey, TypeHash>
)

fun emptyImportBundle() =
    ImportBundle(
        returnTypes = mapOf()
    )

fun parseImport(context: Context): (TokenizedImport) -> Response<ImportBundle> = { tokenizedImport ->
  if (tokenizedImport.path.none()) {
    Response(emptyImportBundle(), listOf())
  } else {
    val path = tokenizedImport.path
        .filter { it.type == BurgType.importPathToken }
        .map { it.value as String }

    val hasWildcard = tokenizedImport.path.last().type == BurgType.importPathWildcard

    if (hasWildcard) {
      val returnTypes = getReturnTypesByPath(context, toPathString(path))

      if (returnTypes.none()) {
        Response(emptyImportBundle(), listOf(ImpError(TextId.importNotFound, burgsToFileRange(tokenizedImport.path))))
      } else {
        val bundle = ImportBundle(
            returnTypes = returnTypes
        )
        Response(bundle, listOf())
      }
    } else {
      val pathKey = toPathKey(path)
      val returnType = getPathKeyTypes(context, pathKey).firstOrNull()
      if (returnType == null)
        Response(emptyImportBundle(), listOf(ImpError(TextId.importNotFound, fileRange = burgsToFileRange(tokenizedImport.path))))
      else {
        val bundle = ImportBundle(
            returnTypes = mapOf(pathKey to returnType)
        )
        Response(bundle, listOf())
      }
    }
  }
}
