package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.OverloadsMap

data class FunctionBundle(
    val interfaces: OverloadsMap,
    val implementation: FunctionImplementationMap
)

fun partitionFunctions(functions: List<CompleteFunction>): FunctionBundle {
    val interfaces = functions
        .associate {
            Pair(
                it.path, mapOf(
                    it.signature to it.parameters
                )
            )
        }
    val implementation = functions
        .associate {
            Pair(FunctionKey(it.path, it.signature), it.implementation)
        }
    return FunctionBundle(
        interfaces = interfaces,
        implementation = implementation
    )
}
