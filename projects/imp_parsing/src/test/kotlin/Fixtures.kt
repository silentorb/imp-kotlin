import silentorb.imp.library.standard.core.intKey
import silentorb.imp.core.Namespace
import silentorb.imp.core.PathKey
import silentorb.imp.core.Type
import silentorb.imp.core.mapTypes

val simpleFunction = Type(
    path = PathKey("silentorb.imp.test", "simpleFunction"),
    types = listOf(intKey, intKey, intKey),
    parameterNames = listOf("first", "second")
)

val somethingFunction = Type(
    path = PathKey("silentorb.imp.test", "something"),
    types = listOf(intKey, intKey, intKey),
    parameterNames = listOf("first", "second")
)

val simpleContext = listOf(
    Namespace(
        types = mapTypes(simpleFunction, somethingFunction)
    )
)
