package execution

import silentorb.imp.core.*
import silentorb.imp.execution.CompleteFunction
import silentorb.imp.execution.newLibrary
import silentorb.imp.library.standard.standardLibrary

fun simpleContext() = listOf(
    defaultImpNamespace(),
    standardLibrary()
)

const val customPath = "imp.test.custom"

val monkeyType = newTypePair(PathKey(customPath, "Monkey"))

fun customLibrary() = newLibrary(
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
        ),
        CompleteFunction(
            path = PathKey(customPath, "newBananaCount"),
            signature = CompleteSignature(
                output = intType
            ),
            implementation = { _ ->
              2
            }
        )
    )
)

fun customTestContext() = listOf(defaultImpNamespace(), customLibrary())
fun standardTestContext() = listOf(defaultImpNamespace(), standardLibrary())
