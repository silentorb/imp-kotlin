package silentorb.imp.library.standard.typing

import silentorb.imp.core.*
import silentorb.imp.execution.CompleteFunction

const val typingPath = "$standardLibraryPath.typing"
val typeType = newTypePair(PathKey(typingPath, "Type"))

//fun typingFunctions() = listOf(
//    CompleteFunction(
//        path = PathKey(typingPath, "union"),
//        signature = CompleteSignature(
//            parameters = listOf(
//                CompleteParameter("first", typeType)
//            ),
//            output = typeType
//        )
//    )
//)
