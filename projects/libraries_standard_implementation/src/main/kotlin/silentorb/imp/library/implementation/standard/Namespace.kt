package silentorb.imp.library.implementation.standard

import silentorb.imp.execution.FunctionImplementationMap
import silentorb.imp.execution.Library
import silentorb.imp.library.implementation.standard.math.mathOperatorImplementations
import silentorb.imp.library.standard.standardLibraryNamespace

fun standardLibraryImplementation(): FunctionImplementationMap =
    mathOperatorImplementations()

fun standardLibrary(): Library =
    Library(
        namespace = standardLibraryNamespace(),
        implementation = standardLibraryImplementation()
    )
