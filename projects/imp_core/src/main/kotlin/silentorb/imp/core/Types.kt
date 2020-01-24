package silentorb.imp.core

typealias Id = String

typealias Type = Any

data class Parameter(
    val name: String,
    val type: Type
)

data class Function(
    val inputs: List<Parameter>,
    val output: Type
)

data class Node(
    val type: Id
)

typealias NodeMap = Map<Id, Node>

data class Connection(
    val output: Id,
    val input: Id,
    val parameter: Id
)

data class NodeInput(
    val node: Id,
    val parameter: Id
)

typealias FunctionMap = Map<Id, Function>

data class Graph(
    val nodes: NodeMap,
    val connections: Set<Connection>,
    val values: Map<NodeInput, Any>
)
