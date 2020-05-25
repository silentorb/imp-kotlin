package silentorb.imp.execution

import silentorb.imp.core.*

data class Library(
    val namespace: Namespace,
    val implementation: FunctionImplementationMap
)

fun convertCompleteSignature(completeSignature: CompleteSignature): Signature =
    Signature(
        parameters = completeSignature.parameters.map { parameter ->
          Parameter(
              parameter.name,
              parameter.type.hash
          )
        },
        output = completeSignature.output.hash
    )

fun newLibrary(functions: List<CompleteFunction>, typeNames: Map<TypeHash, PathKey> = mapOf(), typeAliases: List<TypeAlias> = listOf()): Library {
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

  val extractedTypeNames = functions
      .map { it.signature }
      .fold(mapOf<TypeHash, PathKey>()) { a, signature ->
        a + signature.parameters
            .associate { Pair(it.type.hash, it.type.key) }
            .plus(signature.output.hash to signature.output.key)
      }

  val overloads = interfaces.mapValues { it.value.map(::convertCompleteSignature) }
  val namespace = namespaceFromOverloads(overloads)
  return Library(
      namespace = namespace.copy(
          typeNames = typeNames + extractedTypeNames,
          typings = namespace.typings + newTypings().copy(
              typeAliases = typeAliases
                  .filter { it.alias != null }
                  .associate { Pair(it.path, it.alias!!) },
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
