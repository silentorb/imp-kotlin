package silentorb.imp.library.implementation.standard

import silentorb.imp.execution.FunctionImplementationMap
import silentorb.imp.library.implementation.standard.math.mathOperatorImplementations

fun standardLibraryImplementation(): FunctionImplementationMap =
    mathOperatorImplementations()
