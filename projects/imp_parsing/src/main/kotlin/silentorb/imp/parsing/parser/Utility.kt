package silentorb.imp.parsing.parser

import silentorb.imp.parsing.lexer.Rune

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
