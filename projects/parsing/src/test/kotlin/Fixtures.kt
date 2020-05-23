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
                output = intType
            )
        ),
        PathKey(testPath, "eightPointFive") to listOf(
            Signature(
                parameters = listOf(),
                output = floatType
            )
        ),
        PathKey(testPath, "simpleFunction") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("first", intType),
                    Parameter("second", intType)
                ),
                output = intType
            )
        ),
        PathKey(testPath, "simpleFunction2") to listOf(
            Signature(
                parameters = listOf(
                    Parameter("first", floatType),
                    Parameter("second", intType)
                ),
                output = intType
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
                output = intType
            )
        ),
        vector2iKey to listOf(
            Signature(
                parameters = listOf(
                    Parameter("x", intType),
                    Parameter("y", intType)
                ),
                output = vector2iType
            )
        )
    )) + newNamespace().copy(
        typings = newTypings()
            .copy(
                typeAliases = mapOf(
                    measurementType to floatType
                ),
                numericTypeConstraints = mapOf(
                    measurementType to NumericTypeConstraint(-10.0, 10.5)
                )
            )
    )
)
