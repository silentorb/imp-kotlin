package silentorb.imp.library.implementation.standard.math

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.Key
import silentorb.imp.execution.FunctionImplementation
import silentorb.imp.execution.FunctionImplementationMap
import silentorb.imp.library.standard.math.doubleOperatorSignature
import silentorb.imp.library.standard.math.floatOperatorSignature
import silentorb.imp.library.standard.math.intOperatorSignature
import silentorb.imp.library.standard.math.plusKey

private fun <T> operation(action: (a: T, b: T) -> T): (Map<Key, Any>) -> T =
    { values ->
      val a = values["first"] as T
      val b = values["second"] as T
      action(a, b)
    }

fun mathOperatorImplementations(): FunctionImplementationMap = mapOf(
    FunctionKey(plusKey, intOperatorSignature.hashCode()) to operation<Int> { a, b -> a + b },
    FunctionKey(plusKey, floatOperatorSignature.hashCode()) to operation<Float> { a, b -> a + b },
    FunctionKey(plusKey, doubleOperatorSignature.hashCode()) to operation<Double> { a, b -> a + b }
)
