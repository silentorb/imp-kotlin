package silentorb.imp.library.standard

import silentorb.imp.core.Namespace
import silentorb.imp.execution.newLibrary
import silentorb.imp.library.standard.math.mathFunctions

fun standardLibrary(): Namespace =
    newLibrary(mathFunctions())
