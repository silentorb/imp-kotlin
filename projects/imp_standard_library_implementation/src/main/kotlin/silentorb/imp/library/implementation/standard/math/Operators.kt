package silentorb.imp.library.implementation.standard.math

import silentorb.imp.execution.FunctionImplementationMap
import silentorb.imp.library.standard.math.plusKey

fun mathOperatorImplementations(): FunctionImplementationMap = mapOf(
    plusKey to { values ->
      val first = values["first"] as Int
      val second = values["second"] as Int
      first + second
    }
)
