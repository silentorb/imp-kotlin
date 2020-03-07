package silentorb.imp.core

fun overloadMatches(arguments: List<Argument>, overloads: Signatures): Signatures {
  // TODO: Move this to a better area lower in the stack as a more formal integrity check
  if (arguments.any { it.name == null } && arguments.any { it.name != null })
    return listOf()

  val argumentsByType = arguments.groupBy { it.type }

  return overloads
      .filter { overload ->
        if (arguments.any { it.name == null }) {
          argumentsByType.all { (type, typeArguments) ->
            overload.parameters.count { it.type == type } == typeArguments.size
          }
        } else
          arguments.all { argument -> overload.parameters.firstOrNull { it.name == argument.name!! }?.type == argument.type }
      }
}
//          arguments.zip(overload.parameters).all { (a, b) -> a.type == b.type }
