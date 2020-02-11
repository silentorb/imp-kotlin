package silentorb.imp.core

typealias MapId = (Id) -> Id

fun newChildMap(parent: Set<Id>, child: Set<Id>): MapId {
  val nextId = getNextId(parent)
  val childMap = child
      .mapIndexed { index, id -> Pair(id, nextId + index.toLong()) }
      .associate { it }

  return { childMap[it] ?: it }
}

//fun flattenGraph(parent: Graph, child: Graph, mapId: MapId): Graph {
//  val newNodes = child.nodes.map(mapId)
//
//  val newConnections = child.connections
//      .map { connection ->
//        Connection(
//            source = mapId(connection.source),
//            destination = mapId(connection.destination),
//            parameter = connection.parameter
//        )
//      }
//
//  val newFunctions = child.types
//      .mapKeys { (id, _) -> mapId(id) }
//
//  val newValues = child.values
//      .mapKeys { (id, _) -> mapId(id) }
//
//  return Graph(
//      nodes = parent.nodes.plus(newNodes),
//      connections = parent.connections.plus(newConnections),
//      types = parent.types.plus(newFunctions),
//      values = parent.values.plus(newValues)
//  )
//}

fun mergeDistinctGraphs(parent: Graph, child: Graph): Graph {
  return Graph(
      nodes = parent.nodes.plus(child.nodes),
      connections = parent.connections.plus(child.connections),
      signatures = parent.signatures.plus(child.signatures),
      types = parent.types.plus(child.types),
      values = parent.values.plus(child.values)
  )  
}
