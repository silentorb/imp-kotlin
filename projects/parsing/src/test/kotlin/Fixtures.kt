import silentorb.imp.core.intKey
import silentorb.imp.core.Namespace
import silentorb.imp.core.PathKey

val vector2iKey = PathKey("silentorb.imp.test", "Vector2i")

val simpleContext = listOf(
    Namespace(
        functions = mapOf(
            PathKey("silentorb.imp.test", "simpleFunction") to mapOf(
                listOf(intKey, intKey, intKey) to listOf("first", "second")
            ),
            PathKey("silentorb.imp.test", "something") to mapOf(
                listOf(vector2iKey, vector2iKey) to listOf("first")
            ),
            vector2iKey to mapOf(
                listOf(intKey, intKey, vector2iKey) to listOf("x", "y")
            )
        )
    )
)
