package silentorb.imp.library.standard

import silentorb.imp.core.*
import silentorb.imp.library.standard.math.mathFunctions

fun standardLibraryNamespace(): Namespace {
  val functions = mathFunctions()
  return newNamespace().copy(
      nodeTypes = functions.mapValues { (_, signatures) -> signaturesToTypeHash(signatures) },
      typings = extractTypings(functions)
  )
}
