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
typealias OverloadsMap = Map<PathKey, Signatures>

data class FunctionKey(
    val path: PathKey,
    val signature: Signature
)

data class Connection(
    val destination: Id,
    val source: Id,
    val parameter: Key
)

data class Graph(
    val nodes: Set<Id> = setOf(),
    val connections: Set<Connection> = setOf(),
    val types: Map<Id, PathKey> = mapOf(),
    val signatures: Map<Id, Signature> = mapOf(),
    val values: Map<Id, Any> = mapOf()
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
    val type: PathKey
)
