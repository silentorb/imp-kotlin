package silentorb.imp.core

typealias Key = String
typealias Id = Long

typealias Type = Any

data class Parameter(
    val name: String,
    val type: Type
)

data class Function(
    val inputs: List<Parameter>,
    val output: Type
)

data class Connection(
    val destination: Id,
    val source: Id,
    val parameter: Key
)

typealias Path = String

data class Graph(
    val nodes: Set<Id>,
    val connections: Set<Connection> = setOf(),
    val functions: Map<Id, PathKey>,
    val values: Map<Id, Any>
)

const val defaultParameter = ""

typealias NextId = () -> Id

fun newIdSource(initialValue: Id): NextId {
  var nextId: Id = initialValue
  return { nextId++ }
}
