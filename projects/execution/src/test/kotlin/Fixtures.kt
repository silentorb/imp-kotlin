import silentorb.imp.core.*
import silentorb.imp.execution.CompleteFunction
import silentorb.imp.execution.newLibrary
import silentorb.imp.library.implementation.standard.standardLibraryImplementation
import silentorb.imp.library.standard.standardLibraryNamespace

fun standardLibrary() = standardLibraryImplementation()

fun simpleContext() = listOf(
    standardLibraryNamespace()
)

const val customPath = "imp.test.custom"

val monkeyType = newTypePair(PathKey(customPath, "Monkey"))

val customLibrary = newLibrary(
    listOf(
        CompleteFunction(
            path = PathKey(customPath, "newMonkey"),
            signature = CompleteSignature(
                parameters = listOf(
                    CompleteParameter("bananaCount", intType)
                ),
                output = monkeyType
            ),
            implementation = { arguments ->
              arguments["bananaCount"]!! as Int + 1 // The banana count logic is arbitrary
            }
        )
    )
)

val customLibraryContext = listOf(customLibrary.namespace)
