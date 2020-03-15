package silentorb.imp.core

typealias Key = String
typealias Id = Long

data class Parameter(
    val name: String,
    val type: PathKey
)

data class Signature(
    val parameters: List<Parameter>,
    val output: PathKey
)

typealias Signatures = List<Signature>
typealias SignatureMap = Map<Id, Signature>
typealias OverloadsMap = Map<PathKey, Signatures>

data class SignatureMatch(
    val signature: Signature,
    val alignment: Map<String, Id>
)

typealias SignatureMatchMap = Map<Id, SignatureMatch>

data class FunctionKey(
    val path: PathKey,
    val signature: Signature
)

data class Connection(
    val destination: Id,
    val source: Id,
    val parameter: Key
)

data class Structure(
    val signature: Signature,
    val parameters: List<Key>
)

typealias Union = List<PathKey>

const val defaultParameter = ""

typealias NextId = () -> Id

fun newIdSource(initialValue: Id): NextId {
  var nextId: Id = initialValue
  return { nextId++ }
}

//fun mapTypes(types: List<Function>) =
//    types.associate { Pair(it.key, it) }
//
//fun mapTypes(vararg types: Function) =
//    types.associate { Pair(it.key, it) }

data class Argument(
    val name: String?,
    val type: PathKey,
    val node: Id
)

data class NumericTypeConstraint(
    val minimum: Double,
    val maximum: Double
)

fun newNumericConstraint(minimum: Int, maximum: Int) =
    NumericTypeConstraint(minimum.toDouble(), maximum.toDouble())
