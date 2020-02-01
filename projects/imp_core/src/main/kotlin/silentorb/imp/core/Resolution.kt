package silentorb.imp.core

fun overloadMatches(signature: Signature, overloads: Overloads): Map<Signature, ParameterNames> {
  return overloads
      .filter { overload ->
        signature.zip(overload.key).all { (a, b) -> a == b }
      }
}
