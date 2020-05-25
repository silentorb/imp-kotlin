package silentorb.imp.execution

import silentorb.imp.core.*

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<FunctionKey, FunctionImplementation>

data class CompleteFunction(
    val path: PathKey,
    val signature: CompleteSignature,
    val implementation: FunctionImplementation
)

data class TypeAlias(
    val path: TypeHash,
    val alias: TypeHash? = null,
    val numericConstraint: NumericTypeConstraint? = null
)
