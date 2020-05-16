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
)

//typealias Signatures = List<Signature>
typealias SignatureMap = Map<PathKey, Signature>
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
