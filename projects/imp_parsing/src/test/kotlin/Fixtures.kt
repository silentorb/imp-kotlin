import silentorb.imp.core.intKey
import silentorb.imp.core.Namespace
import silentorb.imp.core.PathKey

val simpleContext = listOf(
    Namespace(
        functions = mapOf(
            PathKey("silentorb.imp.test", "simpleFunction") to mapOf(
                listOf(intKey, intKey, intKey) to listOf("first", "second")
            ),
            PathKey("silentorb.imp.test", "something") to mapOf(
                listOf(intKey, intKey, intKey) to listOf("first", "second")
            )
        )
    )
)
