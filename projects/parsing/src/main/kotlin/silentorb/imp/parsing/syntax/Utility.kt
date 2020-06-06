package silentorb.imp.parsing.syntax

import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.core.newPosition
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.traversing.startExpression

fun newRootBurg(file: TokenFile): Burg =
    Burg(
        type = BurgType.fileRoot,
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

fun adoptChildren(parent: Burg, children: List<Burg>) =
    parent.copy(
        range = parent.range.copy(
            end = children.last().range.end
        ),
        children = parent.children + children.map { it.hashCode() }
    )

fun <T> replaceTop(stack: Stack<T>, newTop: List<T>): Stack<T> =
    stack.dropLast(1).plusElement(newTop)

fun pushMarker(markerType: BurgType, returnMode: ParsingMode? = null) =
    ParsingStep(push(markerType, asMarker), returnMode)

fun logRealmHierarchy(realm: Realm, head: BurgId = realm.root, depth: Int = 0) {
  for (i in 0 until depth) {
    print("  ")
  }
  val burg = realm.burgs[head]!!
  println(burg.type.name)
  for (child in burg.children) {
    logRealmHierarchy(realm, child, depth + 1)
  }
}
