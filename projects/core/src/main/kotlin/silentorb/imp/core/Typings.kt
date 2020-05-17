package silentorb.imp.core

data class Typings(
    val signatures: Map<TypeHash, Signature>,
    val structures: Map<TypeHash, Structure>,
    val unions: Map<TypeHash, Union>
) {
  operator fun plus(other: Typings): Typings =
      mergeTypings(this, other)
}

fun newTypings(): Typings =
    Typings(
        signatures = mapOf(),
        structures = mapOf(),
        unions = mapOf()
    )

fun mergeTypings(first: Typings, second: Typings): Typings =
    Typings(
        signatures = first.signatures + second.signatures,
        structures = first.structures + second.structures,
        unions = first.unions + second.unions
    )

fun reduceTypings(list: List<Typings>): Typings =
    if (list.none())
      newTypings()
    else
      list.reduce(::mergeTypings)

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

fun reduceSignature(signature: Signature, argumentNames: Set<String>): Signature =
    signature.copy(
        parameters = signature.parameters.filter { !argumentNames.contains(it.name) }
    )
