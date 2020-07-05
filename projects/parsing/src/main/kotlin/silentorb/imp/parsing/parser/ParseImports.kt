package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.*
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

fun parseImport(context: Context): (TokenizedImport) -> Response<ImportBundle> = { tokenizedImport ->
  if (tokenizedImport.path.none()) {
    Response(emptyImportBundle(), listOf())
  } else {
    val path = tokenizedImport.path
        .filter { it.type == BurgType.importPathToken }
        .map { it.value as String }

    val hasWildcard = tokenizedImport.path.last().type == BurgType.importPathWildcard

    if (hasWildcard) {
      val returnTypes = getReturnTypes(context, toPathString(path))
      val implementationTypes = getImplementationTypes(context, toPathString(path))

      if (returnTypes.none() && implementationTypes.none()) {
        Response(emptyImportBundle(), listOf(ImpError(TextId.importNotFound, burgsToFileRange(tokenizedImport.path))))
      } else {
        val bundle = ImportBundle(
            implementationTypes = implementationTypes,
            returnTypes = returnTypes
        )
        Response(bundle, listOf())
      }
    } else {
      val pathKey = toPathKey(path)
      val returnType = getPathKeyTypes(context, pathKey).firstOrNull()
      val implementationType = getPathKeyImplementationTypes(context, pathKey).firstOrNull()
      if (returnType == null && implementationType == null)
        Response(emptyImportBundle(), listOf(ImpError(TextId.importNotFound, fileRange = burgsToFileRange(tokenizedImport.path))))
      else {
        val bundle = ImportBundle(
            implementationTypes = if (implementationType != null) mapOf(pathKey to implementationType) else mapOf(),
            returnTypes = if (returnType != null) mapOf(pathKey to returnType) else mapOf()
        )
        Response(bundle, listOf())
      }
    }
  }
}
