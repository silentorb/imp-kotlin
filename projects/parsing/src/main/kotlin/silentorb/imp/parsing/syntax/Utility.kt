package silentorb.imp.parsing.syntax

import silentorb.imp.core.Range
import silentorb.imp.core.newPosition

fun newRootBurg(): Burg =
    Burg(
        type = BurgType.block,
        range = Range(
            start = newPosition(),
            end = newPosition()
        ),
        children = listOf(),
        value = null
    )

fun stackAppend(stack: BurgStack, item: Burg): BurgStack =
    stack.dropLast(1) + stack.last().copy(burgs = stack.last().burgs + item)

fun logRealmHierarchy(realm: Realm, head: Burg = realm.root, depth: Int = 0) {
  for (i in 0 until depth) {
    print("  ")
  }
  val burg = head
  println(burg.type.name + if (burg.value != null) " ${burg.value}" else "")
  for (child in burg.children) {
    logRealmHierarchy(realm, child, depth + 1)
  }
}

fun onMatch(matches: Boolean, getStep: () -> ParsingStep?): ParsingStep? =
    if (matches)
      getStep()
    else
      null

fun rangeFromBurgs(children: List<Burg>): Range =
    Range(
        start = children.map { it.range.start }.minByOrNull { it.index } ?: newPosition(),
        end = children.map { it.range.end }.maxByOrNull { it.index } ?: newPosition()
    )
