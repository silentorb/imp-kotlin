package silentorb.imp.parsing

import silentorb.imp.core.FunctionMap
import silentorb.imp.core.Graph
import silentorb.imp.core.Id
import silentorb.imp.parsing.lexing.Range
import silentorb.imp.parsing.lexing.newPosition
import silentorb.imp.parsing.lexing.tokenize

data class Context(
    val functions: FunctionMap,
    val namespaces: Map<Id, Context>,
    val values: Map<Id, Any>
)

fun emptyContext() = Context(
    functions = mapOf(),
    namespaces = mapOf(),
    values = mapOf()
)

typealias NodeMap = Map<Id, Range>
typealias ValueMap = NodeMap

data class Dungeon(
    val graph: Graph,
    val nodeMap: NodeMap,
    val valueMap: ValueMap
)

fun emptyDungeon() =
    Dungeon(
        graph = Graph(
            nodes = mapOf(),
            connections = setOf(),
            values = mapOf()
        ),
        nodeMap = mapOf(),
        valueMap = mapOf()
    )

fun parseText(context: Context): (String) -> Response<Dungeon> = { code ->
  handle(tokenize(code, position = newPosition(), tokens = listOf())) { tokens ->
    success(emptyDungeon())
  }
}
