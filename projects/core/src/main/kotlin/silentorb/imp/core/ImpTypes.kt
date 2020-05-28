package silentorb.imp.core

const val standardLibraryPath = "imp.standard"

val intKey = PathKey(standardLibraryPath, "Int")

val intType = newTypePair(PathKey(standardLibraryPath, "Int"))
val floatType = newTypePair(PathKey(standardLibraryPath, "Float"))
val doubleType = newTypePair(PathKey(standardLibraryPath, "Double"))

val intSignature = Signature(output = intType.hash)
val floatSignature = Signature(output = floatType.hash)
val doubleSignature = Signature(output = doubleType.hash)
