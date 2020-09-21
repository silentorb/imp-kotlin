package silentorb.imp.parsing.syntax

fun adoptChildren(parent: Burg, children: List<Burg>): Burg =
    if (children.none())
      parent
    else {
      val reduced = children
          .mapNotNull { child ->
            if (child.type == BurgType.application && child.children.size == 1)
              child.children.firstOrNull()?.children?.firstOrNull()
            else
              child
          }

      if (reduced.none())
        parent
      else
        parent.copy(
            range = rangeFromBurgs(children + parent),
            children = parent.children + reduced
        )
    }

fun newBurg(type: BurgType, children: List<Burg>) =
    Burg(
        type = type,
        range = rangeFromBurgs(children),
        children = children
    )

fun shouldCollapseLayer(type: BurgType, children: List<Burg>): Boolean =
    children.size < 2 && setOf(BurgType.application).contains(type)

//val burgs = if (children.size > 0 && layerType is BurgType && (children.size > 1 || layerType != BurgType.application))
//  listOf(newBurg(layerType, children))
//else
//  children
fun popChildren(stack: BurgStack, layerType: Any?, children: List<Burg>): BurgStack {
  val shortStack = stack.dropLast(1)
  val newTop = shortStack.last()
  val burgs = if (children.any() && layerType is BurgType && !shouldCollapseLayer(layerType, children))
    listOf(newBurg(layerType, children))
  else
    children
  return stack.dropLast(2) + newTop.copy(burgs = newTop.burgs + burgs)
}

fun popChildren(state: ParsingState): ParsingState =
    if (state.burgStack.size < 2)
      state
    else {
      val stack = state.burgStack
      val top = stack.last()
      val children = top.burgs
          .filter { it.children.any() || it.value != null } // Empty children are filtered out

      val nextStack = if (children.none())
        stack.dropLast(1)
      else
        popChildren(stack, top.type, children)

      state.copy(
          burgStack = nextStack
      )
    }
