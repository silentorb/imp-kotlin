package silentorb.imp.testing.library

import silentorb.imp.core.CompleteParameter
import silentorb.imp.core.CompleteSignature
import silentorb.imp.core.intType
import silentorb.imp.core.stringType
import silentorb.imp.execution.CompleteFunction

fun testLibraryFunctions() = listOf(
    CompleteFunction(
        path = characterType.key,
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("name", stringType),
                CompleteParameter("intelligence", intType)
            ),
            output = characterType
        ),
        implementation = { arguments ->
          Character(
              name = arguments["name"] as String,
              intelligence = arguments["intelligence"] as Int
          )
        }
    ),
)
