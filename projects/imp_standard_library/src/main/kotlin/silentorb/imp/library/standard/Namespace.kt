package silentorb.imp.library.standard

import silentorb.imp.core.Namespace
import silentorb.imp.library.standard.math.mathFunctions

fun standardLibraryNamespace(): Namespace = Namespace(
    functions = mathFunctions()
)
