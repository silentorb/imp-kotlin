package silentorb.imp.core

fun overloadMatches(arguments: List<Argument>, overloads: Signatures): Signatures {
  return overloads
      .filter { overload ->
        arguments.zip(overload.parameters).all { (a, b) -> a.type == b.type }
      }
}
