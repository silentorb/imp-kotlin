package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.mergeNamespaces
import silentorb.imp.core.namespaceFromOverloads
import silentorb.imp.core.newNamespace

fun newLibrary(functions: List<CompleteFunction>, types: List<TypeAlias> = listOf()): Library {
  val grouped = functions
      .groupBy { it.path }

  val interfaces = grouped
      .mapValues { it.value.map { it.signature } }

  val implementation = grouped.entries
      .flatMap { (path, function) ->
        function.map {
          Pair(FunctionKey(path, it.signature.hashCode()), it.implementation)
        }
      }
      .associate { it }

  return Library(
      namespace = namespaceFromOverloads(interfaces).copy(
          references = types
              .filter { it.alias != null }
              .associate { Pair(it.path, it.alias!!) },
          numericTypeConstraints = types
              .filter { it.numericConstraint != null }
              .associate { Pair(it.path, it.numericConstraint!!) }
      ),
      implementation = implementation
  )
}

fun combineLibraries(vararg libraries: Library): Library =
    Library(
        namespace = mergeNamespaces(libraries.map { it.namespace }),
        implementation = libraries.map { it.implementation }.reduce { a, b -> a.plus(b) }
    )
