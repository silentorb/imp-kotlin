import silentorb.imp.core.*

const val testPath = "silentorb.imp.test"
const val testPath2 = "silentorb.imp.cat"
val vector2iKey = PathKey(testPath, "Vector2i")
val vector2iType = vector2iKey.hashCode()
val measurementKey = PathKey(testPath, "Measurement")
val measurementType = measurementKey.hashCode()

val simpleContext = listOf(
    namespaceFromOverloads(mapOf(
        PathKey(testPath, "eight") to listOf(
            Signature(
                parameters = listOf(),
                output = intType.hash
            )
        ),
        PathKey(testPath, "eightPointFive") to listOf(
            Signature(
                parameters = listOf(),
                output = floatType.hash
            )
        ),
        PathKey(testPath, "simpleFunction") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("first", intType.hash),
                    Parameter("second", intType.hash)
                ),
                output = intType.hash
            )
        ),
        PathKey(testPath, "simpleFunction2") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("first", floatType.hash),
                    Parameter("second", intType.hash)
                ),
                output = intType.hash
            )
        ),
        PathKey(testPath, "something") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("first", vector2iType)
                ),
                output = vector2iType
            )
        ),
        PathKey(testPath, "measure") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("value", measurementType)
                ),
                output = intType.hash
            )
        ),
        vector2iKey to listOf(
            Signature(
                parameters = listOf(
                    Parameter("x", intType.hash),
                    Parameter("y", intType.hash)
                ),
                output = vector2iType
            )
        )
    )) + newNamespace().copy(
        typings = newTypings()
            .copy(
                typeAliases = mapOf(
                    measurementType to floatType.hash
                ),
                numericTypeConstraints = mapOf(
                    measurementType to NumericTypeConstraint(-10.0, 10.5)
                )
            )
    )
)
