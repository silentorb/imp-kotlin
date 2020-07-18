import silentorb.imp.core.*

const val testPath = "silentorb.imp.test"
const val testPath2 = "silentorb.imp.cat"
val vector2iType = newTypePair(PathKey(testPath, "Vector2i"))
val measurementType = newTypePair(PathKey(testPath, "Measurement"))

val simpleContext = listOf(
    defaultImpNamespace(),
    namespaceFromCompleteOverloads(mapOf(
        PathKey(testPath, "eight") to listOf(
            CompleteSignature(
                parameters = listOf(),
                output = intType
            )
        ),
        PathKey(testPath, "eightPointFive") to listOf(
            CompleteSignature(
                parameters = listOf(),
                output = floatType
            )
        ),
        PathKey(testPath, "simpleFunction") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("first", intType),
                    CompleteParameter("second", intType)
                ),
                output = intType
            )
        ),
        PathKey(testPath, "simpleFunction2") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("first", floatType),
                    CompleteParameter("second", intType)
                ),
                output = intType
            )
        ),
        PathKey(testPath, "something") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("first", vector2iType)
                ),
                output = vector2iType
            )
        ),
        PathKey(testPath, "measure") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("value", measurementType)
                ),
                output = intType
            )
        ),
        PathKey(testPath, "overload") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("value", floatType)
                ),
                output = intType
            )
        ),
        PathKey(testPath2, "overload") to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("value", intType)
                ),
                output = intType
            )
        ),
        PathKey(testPath, "add") to listOf(
            CompleteSignature(
                isVariadic = true,
                parameters = listOf(
                    CompleteParameter("value", intType)
                ),
                output = intType
            )
        ),
        vector2iType.key to listOf(
            CompleteSignature(
                parameters = listOf(
                    CompleteParameter("x", intType),
                    CompleteParameter("y", intType)
                ),
                output = vector2iType
            )
        )
    )) + newNamespace().copy(
        typings = newTypings()
            .copy(
                typeAliases = mapOf(
                    measurementType.hash to floatType.hash
                ),
                numericTypeConstraints = mapOf(
                    measurementType.hash to NumericTypeConstraint(-10.0, 10.5)
                )
            )
    ) + nameSpaceFromTypes(listOf(vector2iType))
)
