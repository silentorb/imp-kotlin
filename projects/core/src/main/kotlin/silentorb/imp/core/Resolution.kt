package silentorb.imp.core

fun overloadMatches(arguments: List<Argument>, overloads: Signatures): Signatures {
  // TODO: Move this to a better area lower in the stack as a more formal integrity check
  if (arguments.any { it.name == null } && arguments.any { it.name != null })
    return listOf()

  return overloads
      .filter { overload ->
        if (arguments.any { it.name == null })
          arguments.zip(overload.parameters).all { (a, b) -> a.type == b.type }
        else
          arguments.all { argument -> overload.parameters.firstOrNull { it.name == argument.name!! }?.type == argument.type }
      }
}
