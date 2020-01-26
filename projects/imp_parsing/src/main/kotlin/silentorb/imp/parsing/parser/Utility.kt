package silentorb.imp.parsing.parser

import silentorb.imp.core.flattenGraph
import silentorb.imp.core.mergeDistinctGraphs
import silentorb.imp.core.newChildMap

//fun <T> filterIndicies(collection: Collection<T>, filter: (T) -> Boolean) =
//    collection
//        .foldIndexed(listOf<Int>()) { i, a, b ->
//          if (filter(b))
//            a.plus(i)
//          else
//            a
//        }

fun <T> filterIndicies(list: List<T>, filter: (T) -> Boolean): List<Int> {
  val iterator = list.iterator()
  return list.indices.filter { filter(iterator.next()) }
}

fun <T> nextIndexOf(list: List<T>, start: Int, filter: (T) -> Boolean): Int? {
  val result = list.asSequence().drop(start).indexOfFirst(filter)
  return if (result == -1)
    null
  else
    start + result
}

fun flattenDungeon(parent: Dungeon, child: Dungeon): Dungeon {
  val mapId = newChildMap(parent.graph.nodes, child.graph.nodes)
  val graph = flattenGraph(parent.graph, child.graph, mapId)
  val newNodeMap = child.nodeMap
      .mapKeys { (id, _) -> mapId(id) }

  return Dungeon(
      graph = graph,
      nodeMap = parent.nodeMap.plus(newNodeMap)
  )
}

fun mergeDistinctDungeons(parent: Dungeon, child: Dungeon): Dungeon {
  return Dungeon(
      graph = mergeDistinctGraphs(parent.graph, child.graph),
      nodeMap = parent.nodeMap.plus(child.nodeMap)
  )
}
