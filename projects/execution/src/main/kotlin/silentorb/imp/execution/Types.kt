package silentorb.imp.execution

import silentorb.imp.core.*

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

data class ExecutionStep(
    val node: PathKey,
    val execute: NodeImplementation
)

data class ExecutionUnit(
    val steps: List<ExecutionStep>,
    val values: OutputValues,
    val output: PathKey
)
