import silentorb.imp.core.*

const val testPath = "silentorb.imp.test"
val vector2iKey = PathKey(testPath, "Vector2i")
val measurementKey = PathKey(testPath, "Measurement")

val simpleContext = listOf(
    newNamespace().copy(
        functions = mapOf(
            PathKey(testPath, "eight") to listOf(
                Signature(
                    parameters = listOf(),
                    output = intKey
                )
            ),
            PathKey(testPath, "eightPointFive") to listOf(
                Signature(
                    parameters = listOf(),
                    output = floatKey
                )
            ),
            PathKey(testPath, "simpleFunction") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", intKey),
                        Parameter("second", intKey)
                    ),
                    output = intKey
                )
            ),
            PathKey(testPath, "simpleFunction2") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", floatKey),
                        Parameter("second", intKey)
                    ),
                    output = intKey
                )
            ),
            PathKey(testPath, "something") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", vector2iKey)
                    ),
                    output = vector2iKey
                )
            ),
            PathKey(testPath, "measure") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("value", measurementKey)
                    ),
                    output = intKey
                )
            ),
            vector2iKey to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("x", intKey),
                        Parameter("y", intKey)
                    ),
                    output = vector2iKey
                )
            )
        ),
        references = mapOf(
            measurementKey to floatKey
        ),
        numericTypeConstraints = mapOf(
            measurementKey to NumericTypeConstraint(-10.0, 10.5)
        )
    )
)
