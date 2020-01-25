package silentorb.imp.core

typealias Key = String
typealias AbsolutePath = Key
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

typealias FunctionMap = Map<Key, Function>

data class Graph(
    val nodes: Set<Id>,
    val connections: Set<Connection>,
    val functions: Map<Id, AbsolutePath>,
    val values: Map<Id, Any>
)

data class Context(
    val functions: FunctionMap,
    val namespaces: Map<Key, Context>,
    val values: Map<Key, Any>
)

const val defaultParameter = ""
