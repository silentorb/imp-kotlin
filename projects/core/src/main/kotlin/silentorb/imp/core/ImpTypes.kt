package silentorb.imp.core

const val standardLibraryPath = "imp"

val intKey = PathKey(standardLibraryPath, "Int")

val intType = newTypePair(PathKey(standardLibraryPath, "Int"))
val floatType = newTypePair(PathKey(standardLibraryPath, "Float"))
val doubleType = newTypePair(PathKey(standardLibraryPath, "Double"))

val intSignature = Signature(output = intType.hashCode())
val floatSignature = Signature(output = floatType.hashCode())
val doubleSignature = Signature(output = doubleType.hashCode())
