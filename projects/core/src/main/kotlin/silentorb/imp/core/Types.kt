package silentorb.imp.core

typealias Key = String
typealias TypeHash = Int

data class PathKey(
    val path: String,
    val name: String,
    val type: TypeHash? = null
) {
  init {
    assert(path.firstOrNull() != '.')
    if (path.contains("E:")) {
      val j = 0
    }
  }
}

typealias Union = Set<TypeHash>

data class Parameter(
    val name: String,
    val type: TypeHash
)

// When overriding Kotlin hashCode, no hashing code is auto-generated.
// HashSignature exists so that Kotlin will still auto-generate hashing code
// that Signature's custom hashCode function can fall back to.
data class HashSignature(
    val parameters: List<Parameter> = listOf(),
    val output: TypeHash
)

data class Signature(
    val parameters: List<Parameter> = listOf(),
    val output: TypeHash,
    val isVariadic: Boolean
) {
  override fun hashCode(): Int {
    return if (parameters.none() && !isVariadic)
      output
    else
      HashSignature(parameters, output).hashCode()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Signature

    if (parameters != other.parameters) return false
    if (output != other.output) return false
    if (isVariadic != other.isVariadic) return false

    return true
  }
}

typealias SignatureMap = Map<TypeHash, Signature>
typealias OverloadsMap = Map<PathKey, List<Signature>>

data class SignatureMatch(
    val signature: Signature,
    val alignment: Map<String, PathKey>,
    val complete: Boolean
)

typealias SignatureMatchMap = Map<PathKey, SignatureMatch>

data class FunctionKey(
    val key: PathKey,
    val type: TypeHash
)

data class Input(
    val destination: PathKey,
    val parameter: Key
)

typealias Connections = Map<Input, PathKey>

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
    val output: TypePair,
    val isVariadic: Boolean = false
) {
  init {
    assert(!isVariadic || parameters.size == 1)
  }
}

const val unknownSymbol = "Unknown"
const val anySymbol = "Any"

val unknownType = newTypePair(PathKey("", unknownSymbol))
val anyType = newTypePair(PathKey("", anySymbol))

typealias FunctionImplementation = (Map<Key, Any>) -> Any
typealias FunctionImplementationMap = Map<FunctionKey, FunctionImplementation>

typealias NodeImplementationArguments = Map<PathKey, Any>
typealias NodeImplementation = (NodeImplementationArguments) -> Any
