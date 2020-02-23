package silentorb.imp.library.standard.math

import silentorb.imp.core.*

const val mathPath = "$standardLibraryPath.math"

val plusKey = PathKey(mathPath, "+")

fun mathOperatorSignature(type: PathKey) = Signature(
    parameters = listOf(
        Parameter("first", type),
        Parameter("second", type)
    ),
    output = type
)

val intOperatorSignature = mathOperatorSignature(intKey)
val floatOperatorSignature = mathOperatorSignature(floatKey)
val doubleOperatorSignature = mathOperatorSignature(doubleKey)

fun standardMathOperatorDefinition() =
    listOf(
        intOperatorSignature,
        floatOperatorSignature,
        doubleOperatorSignature
    )

fun mathOperators(): OverloadsMap {
  val definition = standardMathOperatorDefinition()
  return setOf(plusKey)
      .associateWith { definition }
}
