package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.*

val emptyContext: Context = listOf(newNamespace())

//fun <T> filterIndicies(collection: Collection<T>, filter: (T) -> Boolean) =
//    collection
//        .foldIndexed(listOf<Int>()) { i, a, b ->
//          if (filter(b))
//            a.plus(i)
//          else
//            a
//        }

fun <T> filterIndices(list: List<T>, filter: (T) -> Boolean): List<Int> {
  val iterator = list.iterator()
  return list.indices.filter { filter(iterator.next()) }
}

fun <T> split(list: List<T>, dividers: List<Int>): List<List<T>> {
  val indices = listOf(-1).plus(dividers)
  return indices.mapIndexed { index, start ->
    val end = indices.getOrElse(index + 1) { list.size }
    list.subList(start + 1, end)
  }
}

fun <T> split(list: List<T>, divider: (T) -> Boolean): List<List<T>> =
    split(list, filterIndices(list, divider))

fun <T> nextIndexOf(list: List<T>, start: Int, filter: (T) -> Boolean): Int? {
  val result = list.asSequence().drop(start).indexOfFirst(filter)
  return if (result == -1)
    null
  else
    start + result
}

fun <T> untilIndex(list: List<T>, filter: (T) -> Boolean): Int {
  val result = list.indexOfFirst(filter)
  return if (result == -1)
    list.size
  else
    result
}

fun <T> until(list: List<T>, filter: (T) -> Boolean): List<T> =
    list.take(untilIndex(list, filter))

//fun flattenDungeon(parent: Dungeon, child: Dungeon): Dungeon {
//  val mapId = newChildMap(parent.graph.nodes, child.graph.nodes)
//  val graph = flattenGraph(parent.graph, child.graph, mapId)
//  val newNodeMap = child.nodeMap
//      .mapKeys { (id, _) -> mapId(id) }
//
//  return Dungeon(
//      graph = graph,
//      nodeMap = parent.nodeMap.plus(newNodeMap)
//  )
//}

fun mergeDistinctDungeons(parent: Dungeon, child: Dungeon): Dungeon {
  return Dungeon(
      graph = mergeNamespaces(parent.graph, child.graph),
      nodeMap = parent.nodeMap.plus(child.nodeMap),
      literalConstraints = mapOf()
  )
}

fun <T> filterIndexes(collection: Collection<T>, predicate: (T) -> Boolean): List<Int> =
    collection.mapIndexedNotNull { index, token ->
      if (predicate(token))
        index
      else
        null
    }
