package silentorb.imp.core

const val standardLibraryPath = "imp"

val intKey = PathKey(standardLibraryPath, "Int")
val floatKey = PathKey(standardLibraryPath, "Float")
val doubleKey = PathKey(standardLibraryPath, "Double")

val intSignature = Signature(output = intKey.hashCode())
val floatSignature = Signature(output = floatKey.hashCode())
val doubleSignature = Signature(output = doubleKey.hashCode())
