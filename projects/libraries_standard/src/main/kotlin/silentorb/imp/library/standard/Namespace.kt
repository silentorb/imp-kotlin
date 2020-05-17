package silentorb.imp.library.standard

import silentorb.imp.core.*
import silentorb.imp.library.standard.math.mathFunctions

fun standardLibraryNamespace(): Namespace =
    namespaceFromOverloads(mathFunctions())
