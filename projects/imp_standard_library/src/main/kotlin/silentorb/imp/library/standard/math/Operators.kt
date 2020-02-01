package silentorb.imp.library.standard.math

import silentorb.imp.library.standard.core.intKey
import silentorb.imp.core.PathKey
import silentorb.imp.core.Type
import silentorb.imp.library.standard.standardLibraryPath

const val mathPath = "$standardLibraryPath.math"

val plusKey = PathKey(mathPath, "+")

val plusType = Type(
    path = plusKey,
    types = listOf(intKey, intKey),
    parameterNames = listOf("first", "second")
)
