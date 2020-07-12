package silentorb.imp.execution

import silentorb.imp.core.*

data class Library(
    val namespace: Namespace,
    val implementation: FunctionImplementationMap
)

fun newLibrary(functions: List<CompleteFunction>, typeNames: Map<TypeHash, PathKey> = mapOf(), typeAliases: List<TypeAlias> = listOf()): Library {
  val grouped = functions
      .groupBy { it.path }

  val signatures = grouped
      .mapValues { it.value.map { it.signature } }

  val implementation = grouped.entries
      .flatMap { (path, function) ->
        function.map {
          val signature = convertCompleteSignature(it.signature)
          Pair(FunctionKey(path, signature.hashCode()), it.implementation)
        }
      }
      .associate { it }

  val namespace = namespaceFromCompleteOverloads(signatures)
  return Library(
      namespace = namespace.copy(
          nodeTypes = namespace.nodeTypes + typeNames.entries.associate { it.value to it.key },
          typings = namespace.typings + newTypings().copy(
              typeAliases = typeAliases
                  .filter { it.alias != null }
                  .associate { Pair(it.path, it.alias!!) },
              typeNames = typeNames + namespace.typings.typeNames,
              numericTypeConstraints = typeAliases
                  .filter { it.numericConstraint != null }
                  .associate { Pair(it.path, it.numericConstraint!!) }
          )
      ),
      implementation = implementation
  )
}

fun combineLibraries(vararg libraries: Library): Library =
    Library(
        namespace = mergeNamespaces(libraries.map { it.namespace }),
        implementation = libraries.map { it.implementation }.reduce { a, b -> a.plus(b) }
    )

fun typePairstoTypeNames(typePairs: List<TypePair>) =
    typePairs.associate { Pair(it.hash, it.key) }
