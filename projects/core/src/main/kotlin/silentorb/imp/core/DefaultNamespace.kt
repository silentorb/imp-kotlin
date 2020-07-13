package silentorb.imp.core

fun defaultImpNamespace(): Namespace {
  val mathTypes = newMathTypes()
  val signatures = mathTypes
      .associate {
        val signature = Signature(
            isVariadic = false,
            parameters = listOf(),
            output = it.hash
        )
        signature.hashCode() to signature
      }

  return newNamespace().copy(
      nodeTypes = mathTypes.associate { it.key to it.hash },
      typings = newTypings().copy(
          signatures = signatures,
          typeNames = mathTypes.associate { it.hash to it.key }
      )
  )
}

