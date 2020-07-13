package silentorb.imp.execution

import silentorb.imp.core.*

fun newLibrary(functions: List<CompleteFunction>, typeNames: Map<TypeHash, PathKey> = mapOf(), typeAliases: List<TypeAlias> = listOf()): Namespace {
  val grouped = functions
      .groupBy { it.path }

  val signatures = grouped
      .mapValues { it.value.map { it.signature } }

  val implementation = grouped.entries
      .flatMap { (path, function) ->
        function.map {
          val signature = convertCompleteSignature(it.signature)
          Pair(path.copy(type = signature.hashCode()), it.implementation)
        }
      }
      .associate { it }

  val namespace = namespaceFromCompleteOverloads(signatures)
  return namespace.copy(
      values = namespace.values + implementation,
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
  )
}

fun typePairstoTypeNames(typePairs: List<TypePair>) =
    typePairs.associate { Pair(it.hash, it.key) }
