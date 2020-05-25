package silentorb.imp.core

data class Typings(
    val numericTypeConstraints: Map<TypeHash, NumericTypeConstraint>,
    val signatures: Map<TypeHash, Signature>,
    val structures: Map<TypeHash, Structure>,
    val typeAliases: Map<TypeHash, TypeHash>,
    val typeNames: Map<TypeHash, PathKey>,
    val unions: Map<TypeHash, Union>
) {
  operator fun plus(other: Typings): Typings =
      mergeTypings(this, other)
}

fun newTypings(): Typings =
    Typings(
        numericTypeConstraints = mapOf(),
        signatures = mapOf(),
        structures = mapOf(),
        typeAliases = mapOf(),
        typeNames = mapOf(),
        unions = mapOf()
    )

fun mergeTypings(first: Typings, second: Typings): Typings =
    Typings(
        numericTypeConstraints = first.numericTypeConstraints + second.numericTypeConstraints,
        signatures = first.signatures + second.signatures,
        structures = first.structures + second.structures,
        typeAliases = first.typeAliases + second.typeAliases,
        typeNames = first.typeNames + second.typeNames,
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
