package silentorb.imp.library.standard.math

import silentorb.imp.core.*
import silentorb.imp.execution.CompleteFunction

const val mathPath = "$standardLibraryPath.math"

val plusKey = PathKey(mathPath, "+")

fun mathOperatorSignature(type: TypePair) = CompleteSignature(
    parameters = listOf(
        CompleteParameter("first", type),
        CompleteParameter("second", type)
    ),
    output = type
)

val intOperatorSignature = mathOperatorSignature(intType)
val floatOperatorSignature = mathOperatorSignature(floatType)
val doubleOperatorSignature = mathOperatorSignature(doubleType)

fun <T> operationImplementation(action: (a: T, b: T) -> T): (Map<Key, Any>) -> T =
    { values ->
      val a = values["first"] as T
      val b = values["second"] as T
      action(a, b)
    }

fun mathOperators() = listOf(
    CompleteFunction(
        path = PathKey(mathPath, "+"),
        signature = intOperatorSignature,
        implementation = operationImplementation<Int> { a, b -> a + b }
    ),
    CompleteFunction(
        path = PathKey(mathPath, "+"),
        signature = floatOperatorSignature,
        implementation = operationImplementation<Float> { a, b -> a + b }
    ),
    CompleteFunction(
        path = PathKey(mathPath, "+"),
        signature = doubleOperatorSignature,
        implementation = operationImplementation<Double> { a, b -> a + b }
    )
)
