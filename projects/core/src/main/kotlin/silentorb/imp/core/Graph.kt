package silentorb.imp.core

data class Typings(
  val signatures: Map<TypeHash, Signature>,
  val unions: Map<TypeHash, Union>
)

data class Namespace(
    val connections: Set<Connection>,
    val references: Map<PathKey, PathKey>,
    val nodeTypes: Map<PathKey, TypeHash>,
    val values: Map<PathKey, Any>,
    val structures: Map<PathKey, Structure>,
    val typings: Typings,
    val numericTypeConstraints: Map<PathKey, NumericTypeConstraint>
)

typealias Graph = Namespace
