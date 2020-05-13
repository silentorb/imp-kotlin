package silentorb.imp.core

typealias Key = String
//typealias Id = Long

data class Parameter(
    val name: String,
    val type: PathKey
)

data class Signature(
    val parameters: List<Parameter>,
    val output: PathKey
)

typealias Signatures = List<Signature>
typealias SignatureMap = Map<PathKey, Signature>
typealias OverloadsMap = Map<PathKey, Signatures>
typealias Type = List<PathKey>

data class SignatureMatch(
    val signature: Signature,
    val alignment: Map<String, PathKey>
)

typealias SignatureMatchMap = Map<PathKey, SignatureMatch>

data class FunctionKey(
    val path: PathKey,
    val signature: Signature
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

typealias Union = List<PathKey>

const val defaultParameter = ""

//fun newIdSource(initialValue: PathKey): NextId {
//  var nextId: PathKey = initialValue
//  return { nextId++ }
//}

//fun mapTypes(types: List<Function>) =
//    types.associate { Pair(it.key, it) }
//
//fun mapTypes(vararg types: Function) =
//    types.associate { Pair(it.key, it) }

data class Argument(
    val name: String?,
    val type: PathKey,
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
