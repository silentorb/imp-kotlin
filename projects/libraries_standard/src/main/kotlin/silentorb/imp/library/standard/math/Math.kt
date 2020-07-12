package silentorb.imp.library.standard.math

import silentorb.imp.core.doubleType
import silentorb.imp.core.floatType
import silentorb.imp.core.intType

fun mathFunctions() =
    mathOperators()

fun mathTypeNames() = listOf(
    intType,
    floatType,
    doubleType
)
    .associate { it.hash to it.key }
