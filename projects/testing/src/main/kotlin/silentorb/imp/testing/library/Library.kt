package silentorb.imp.testing.library

import silentorb.imp.core.Namespace
import silentorb.imp.execution.newLibrary

fun newTestLibrary(): Namespace =
    newLibrary(
        functions = testLibraryFunctions(),
        typeNames = testLibraryTypes()
    )
