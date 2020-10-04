package silentorb.imp.testing.library

import silentorb.imp.core.PathKey
import silentorb.imp.core.newTypePair
import silentorb.imp.execution.typePairsToTypeNames

data class Character(
    val name: String,
    val intelligence: Int
)

const val testingPath = "silentorb.imp.testing.library"

val characterType = newTypePair(PathKey(testingPath, "Character"))

fun testLibraryTypes() =
    typePairsToTypeNames(
        listOf(
            characterType
        )
    )
