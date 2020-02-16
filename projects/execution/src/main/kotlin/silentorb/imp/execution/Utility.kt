package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.Namespace
import silentorb.imp.core.OverloadsMap
import silentorb.imp.core.combineNamespaces

fun partitionFunctions(functions: List<CompleteFunction>): Library {
  val interfaces = functions
      .associate {
        Pair(
            it.path, mapOf(
            it.signature to it.parameters
        )
        )
      }
  val implementation = functions
      .associate {
        Pair(FunctionKey(it.path, it.signature), it.implementation)
      }
  return Library(
      namespace = Namespace(
          functions = interfaces
      ),
      implementation = implementation
  )
}

fun combineLibraries(vararg libraries: Library): Library =
    Library(
        namespace = combineNamespaces(libraries.map { it.namespace }),
        implementation = libraries.map { it.implementation }.reduce { a, b -> a.plus(b) }
    )
