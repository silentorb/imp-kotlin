package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.syntax.*

val emptyContext: Context = listOf(newNamespace())

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

fun mergeDungeons(parent: Dungeon, child: Dungeon): Dungeon {
  return Dungeon(
      namespace = parent.namespace + child.namespace,
      nodeMap = parent.nodeMap + child.nodeMap,
      implementationGraphs = parent.implementationGraphs + child.implementationGraphs
  )
}

fun mergeDungeons(dungeons: List<Dungeon>): Dungeon? =
    if (dungeons.size < 2)
      dungeons.firstOrNull()
    else
      dungeons.reduce(::mergeDungeons)

fun <T> filterIndexes(collection: Collection<T>, predicate: (T) -> Boolean): List<Int> =
    collection.mapIndexedNotNull { index, token ->
      if (predicate(token))
        index
      else
        null
    }

fun getChildren(realm: Realm, parent: BurgId): List<BurgId> =
    (realm.burgs[parent]?.children ?: listOf())

fun getExpandedChildren(realm: Realm, parent: BurgId): List<Burg> =
    (realm.burgs[parent]?.children ?: listOf())
        .map { realm.burgs[it]!! }

fun subRealm(roads: Roads, root: BurgId, depth: Int = 1): Set<BurgId> =
    if (depth > 100)
      throw Error("Infinite loop while parsing")
    else {
      val children = roads[root] ?: listOf()
      children
          .flatMap { subRealm(roads, it, depth + 1) }
          .plus(root)
          .toSet()
    }
