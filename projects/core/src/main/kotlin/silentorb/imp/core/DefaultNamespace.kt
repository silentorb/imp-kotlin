package silentorb.imp.core

fun nameSpaceFromTypes(types: List<TypePair>): Namespace {
  val signatures = types
      .associate {
        val signature = Signature(
            isVariadic = false,
            parameters = listOf(),
            output = it.hash
        )
        signature.hashCode() to signature
      }

  return newNamespace().copy(
      nodeTypes = types.associate { it.key.copy(type = it.hash) to it.hash },
      typings = newTypings().copy(
          signatures = signatures,
          typeNames = types.associate { it.hash to it.key.name }
      )
  )
}

fun defaultImpNamespace(): Namespace =
    nameSpaceFromTypes(newMathTypes() + newCoreTypes())
