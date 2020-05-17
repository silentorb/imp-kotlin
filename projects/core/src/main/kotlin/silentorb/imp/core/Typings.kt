package silentorb.imp.core

data class Typings(
    val signatures: Map<TypeHash, Signature>,
    val unions: Map<TypeHash, Union>
)

fun mergeTypings(first: Typings, second: Typings): Typings =
    Typings(
        signatures = first.signatures + second.signatures,
        unions = first.unions + second.unions
    )

fun extractTypings(signatures: List<Signature>): Typings {
  val signatureMap = signatures
      .associateBy { it.hashCode() }

  val unions = if (signatures.size == 1)
    mapOf()
  else {
    val types = signatureMap.keys
    mapOf(types.hashCode() to types)
  }
  return newTypings().copy(
      signatures = signatureMap,
      unions = unions
  )
}

fun extractTypings(signatures: Collection<List<Signature>>): Typings {
  return signatures
      .map(::extractTypings)
      .reduce(::mergeTypings)
}

