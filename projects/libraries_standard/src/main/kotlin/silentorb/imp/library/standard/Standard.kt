package silentorb.imp.library.standard

import silentorb.imp.execution.Library
import silentorb.imp.execution.newLibrary
import silentorb.imp.library.standard.math.mathFunctions

fun standardLibrary(): Library =
    newLibrary(mathFunctions())
