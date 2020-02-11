package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.Key
import silentorb.imp.core.PathKey
import silentorb.imp.core.Signature

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<FunctionKey, FunctionImplementation>

data class CompleteFunction(
    val path: PathKey,
    val signature: Signature,
    val parameters: List<String>,
    val implementation: FunctionImplementation
)
