package silentorb.imp.core

typealias Key = String
typealias Id = Long

typealias Signature = List<PathKey>
typealias ParameterNames = List<String>

typealias Function = Map.Entry<Signature, ParameterNames>
typealias Overloads = Map<Signature, ParameterNames>
typealias OverloadsMap = Map<PathKey, Overloads>

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
    val nodes: Set<Id>,
    val connections: Set<Connection> = setOf(),
    val types: Map<Id, PathKey>,
    val signatures: Map<Id, Signature> = mapOf(),
    val values: Map<Id, Any>
)

const val defaultParameter = ""

typealias NextId = () -> Id

fun newIdSource(initialValue: Id): NextId {
  var nextId: Id = initialValue
  return { nextId++ }
}

fun mapTypes(types: List<Function>) =
    types.associate { Pair(it.key, it) }

fun mapTypes(vararg types: Function) =
    types.associate { Pair(it.key, it) }
