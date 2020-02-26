import silentorb.imp.core.*

val vector2iKey = PathKey("silentorb.imp.test", "Vector2i")

val simpleContext = listOf(
    Namespace(
        functions = mapOf(
            PathKey("silentorb.imp.test", "simpleFunction") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", intKey),
                        Parameter("second", intKey)
                    ),
                    output = intKey
                )
            ),
            PathKey("silentorb.imp.test", "simpleFunction2") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", floatKey),
                        Parameter("second", intKey)
                    ),
                    output = intKey
                )
            ),
            PathKey("silentorb.imp.test", "something") to listOf(
                Signature(
                    parameters = listOf(
                        Parameter("first", vector2iKey)
                    ),
                    output = vector2iKey
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
        )
    )
)
