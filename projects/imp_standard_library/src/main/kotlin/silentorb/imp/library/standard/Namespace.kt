package silentorb.imp.library.standard

import silentorb.imp.library.standard.math.mathTypes
import silentorb.imp.core.Namespace
import silentorb.imp.core.mapTypes

const val standardLibraryPath = "imp"

fun standardLibraryNamespace(): Namespace = Namespace(
    types = mapTypes(
        mathTypes()
    )
)
