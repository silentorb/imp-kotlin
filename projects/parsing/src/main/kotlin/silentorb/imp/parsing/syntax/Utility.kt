package silentorb.imp.parsing.syntax

import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.core.newPosition

fun newRootBurg(file: TokenFile): Burg =
    Burg(
        type = BurgType.block,
        file = file,
        range = Range(
            start = newPosition(),
            end = newPosition()
        ),
        children = listOf(),
        value = null
    )

fun <T> stackAppend(stack: Stack<T>, item: T): Stack<T> =
    stack.dropLast(1).plusElement(stack.last() + item)

fun adoptChildren(burgs: Set<Burg>, parent: Burg, children: List<Burg>): Burg =
    throw Error("No longer supported")
//    if (children.none())
//      parent
//    else {
//      val reduced = children
//          .map { child ->
//            if (child.type == BurgType.application && child.children.size == 1)
//              burgs.first {it.hashCode() == child.children.first()}.children.first()
//            else
//              child.hashCode()
//          }
//
//      parent.copy(
//          range = parent.range.copy(
//              start = children.map { it.range.start }.plus(parent.range.start).minBy { it.index }!!,
//              end = children.map { it.range.end }.plus(parent.range.end).maxBy { it.index }!!
//          ),
//          children = parent.children + reduced
//      )
//    }

fun <T> replaceTop(stack: Stack<T>, newTop: List<T>): Stack<T> =
    stack.dropLast(1).plusElement(newTop)

fun pushMarker(markerType: BurgType, returnMode: ParsingMode) =
    push(markerType, asMarker) + goto(returnMode)

//fun logRealmHierarchy(realm: Realm, head: BurgId = realm.root, depth: Int = 0) {
//  for (i in 0 until depth) {
//    print("  ")
//  }
//  val burg = realm.burgs[head]!!
//  println(burg.type.name + if (burg.value != null) " ${burg.value}" else "")
//  for (child in burg.children) {
//    logRealmHierarchy(realm, child, depth + 1)
//  }
//}

fun onMatch(matches: Boolean, getStep: () -> ParsingStep?): ParsingStep? =
    if (matches)
      getStep()
    else
      null
