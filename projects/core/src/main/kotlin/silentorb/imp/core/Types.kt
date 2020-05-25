package silentorb.imp.core

typealias Key = String
typealias TypeHash = Int

data class PathKey(
    val path: String,
    val name: String
)

typealias Union = Set<TypeHash>

data class Parameter(
    val name: String,
    val type: TypeHash
)

data class Signature(
    val parameters: List<Parameter> = listOf(),
    val output: TypeHash
) {
  override fun hashCode(): Int {
    return if (parameters.none())
      output
    else
      super.hashCode()

  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Signature

    if (parameters != other.parameters) return false
    if (output != other.output) return false

    return true
  }
}

typealias SignatureMap = Map<TypeHash, Signature>
typealias OverloadsMap = Map<PathKey, List<Signature>>

data class SignatureMatch(
    val signature: Signature,
    val alignment: Map<String, PathKey>
)

typealias SignatureMatchMap = Map<PathKey, SignatureMatch>

data class FunctionKey(
    val key: PathKey,
    val type: TypeHash
)

data class Connection(
    val destination: PathKey,
    val source: PathKey,
    val parameter: Key
)

data class Structure(
    val signature: Signature,
    val parameters: List<Key>
)

const val defaultParameter = ""

data class Argument(
    val name: String?,
    val type: TypeHash,
    val node: PathKey
)

data class NumericTypeConstraint(
    val minimum: Double,
    val maximum: Double
)

fun newNumericConstraint(minimum: Int, maximum: Int) =
    NumericTypeConstraint(minimum.toDouble(), maximum.toDouble())

fun newNumericConstraint(minimum: Float, maximum: Float) =
    NumericTypeConstraint(minimum.toDouble(), maximum.toDouble())

fun formatPathKey(key: PathKey): String =
    if (key.path.isEmpty())
      key.name
    else
      "${key.path}.${key.name}"

data class TypePair(
    val hash: TypeHash,
    val key: PathKey
)

fun newTypePair(key: PathKey) =
    TypePair(
        hash = key.hashCode(),
        key = key
    )

data class CompleteParameter(
    val name: String,
    val type: TypePair
)

data class CompleteSignature(
    val parameters: List<CompleteParameter> = listOf(),
    val output: TypePair
)

const val unknownSymbol = "unknown"
val unknownKey = PathKey("", unknownSymbol)
