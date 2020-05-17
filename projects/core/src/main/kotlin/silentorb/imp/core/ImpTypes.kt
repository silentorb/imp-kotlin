package silentorb.imp.core

const val standardLibraryPath = "imp"

val intKey = PathKey(standardLibraryPath, "Int")
val floatKey = PathKey(standardLibraryPath, "Float")
val doubleKey = PathKey(standardLibraryPath, "Double")

val intType = intKey.hashCode()
val floatType = floatKey.hashCode()
val doubleType = doubleKey.hashCode()

val intSignature = Signature(output = intType.hashCode())
val floatSignature = Signature(output = floatType.hashCode())
val doubleSignature = Signature(output = doubleType.hashCode())
