package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.mergeNamespaces
import silentorb.imp.core.newNamespace

fun partitionFunctions(functions: List<CompleteFunction>): Library {
  val grouped = functions
      .groupBy { it.path }

  val interfaces = grouped
      .mapValues { it.value.map { it.signature } }

  val implementation = grouped.entries
      .flatMap { (path, function) ->
        function.map {
          Pair(FunctionKey(path, it.signature), it.implementation)
        }
      }
      .associate { it }
  
  return Library(
      namespace = newNamespace().copy(
          functions = interfaces
      ),
      implementation = implementation
  )
}

fun combineLibraries(vararg libraries: Library): Library =
    Library(
        namespace = mergeNamespaces(libraries.map { it.namespace }),
        implementation = libraries.map { it.implementation }.reduce { a, b -> a.plus(b) }
    )
