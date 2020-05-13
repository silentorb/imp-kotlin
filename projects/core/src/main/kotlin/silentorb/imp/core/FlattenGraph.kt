package silentorb.imp.core

typealias MapId = (PathKey) -> PathKey

//fun newChildMap(parent: Set<PathKey>, child: Set<PathKey>): MapId {
//  val nextId = getNextId(parent)
//  val childMap = child
//      .mapIndexed { index, id -> Pair(id, nextId + index.toLong()) }
//      .associate { it }
//
//  return { childMap[it] ?: it }
//}

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
      signatureMatches = parent.signatureMatches.plus(child.signatureMatches),
      references = parent.references.plus(child.references),
      outputTypes = parent.outputTypes.plus(child.outputTypes),
      values = parent.values.plus(child.values)
  )  
}
