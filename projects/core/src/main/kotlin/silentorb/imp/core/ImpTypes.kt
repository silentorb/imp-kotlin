package silentorb.imp.core

const val standardLibraryPath = "imp.standard"

val intType = newTypePair(PathKey(standardLibraryPath, "Int"))
val floatType = newTypePair(PathKey(standardLibraryPath, "Float"))
val doubleType = newTypePair(PathKey(standardLibraryPath, "Double"))

fun newMathTypes() = listOf(
    intType,
    floatType,
    doubleType
)
