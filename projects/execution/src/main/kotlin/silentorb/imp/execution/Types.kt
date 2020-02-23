package silentorb.imp.execution

import silentorb.imp.core.*

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<FunctionKey, FunctionImplementation>

data class CompleteFunction(
    val path: PathKey,
    val signature: Signature,
    val implementation: FunctionImplementation
)

data class Library(
    val namespace: Namespace,
    val implementation: FunctionImplementationMap
)
