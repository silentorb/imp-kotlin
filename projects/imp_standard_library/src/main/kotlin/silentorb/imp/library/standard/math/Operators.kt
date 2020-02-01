package silentorb.imp.library.standard.math

import silentorb.imp.core.*

const val mathPath = "$standardLibraryPath.math"

val plusKey = PathKey(mathPath, "+")

fun standardMathOperatorDefinition() =
    mapOf(
        listOf(intKey, intKey, intKey) to listOf("first", "second"),
        listOf(floatKey, floatKey, floatKey) to listOf("first", "second"),
        listOf(doubleKey, doubleKey, doubleKey) to listOf("first", "second")
    )

fun mathOperators(): OverloadsMap {
  val definition = standardMathOperatorDefinition()
  return setOf(plusKey)
      .associateWith { definition }
}
