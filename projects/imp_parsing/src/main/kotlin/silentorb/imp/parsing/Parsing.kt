package silentorb.imp.parsing

import silentorb.imp.core.FunctionMap
import silentorb.imp.core.Graph
import silentorb.imp.core.Id

data class Context(
    val values: Map<Id, Any>,
    val functions: FunctionMap
)

fun parseText(context: Context): (String) -> Graph = { text ->
  Graph(
      nodes = mapOf(),
      connections = setOf(),
      values = mapOf()
  )
}
