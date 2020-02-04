package silentorb.imp.library.standard.math

import silentorb.imp.core.*

const val mathPath = "$standardLibraryPath.math"

val plusKey = PathKey(mathPath, "+")

val intOperatorSignature = listOf(intKey, intKey, intKey)
val floatOperatorSignature = listOf(floatKey, floatKey, floatKey)
val doubleOperatorSignature = listOf(doubleKey, doubleKey, doubleKey)

fun standardMathOperatorDefinition() =
    mapOf(
        intOperatorSignature to listOf("first", "second"),
        floatOperatorSignature to listOf("first", "second"),
        doubleOperatorSignature to listOf("first", "second")
    )

fun mathOperators(): OverloadsMap {
  val definition = standardMathOperatorDefinition()
  return setOf(plusKey)
      .associateWith { definition }
}
