package silentorb.imp.core

data class Graph(
    val nodes: Set<PathKey> = setOf(),
    val connections: Set<Connection> = setOf(),
    val references: Map<PathKey, PathKey> = mapOf(),
    val signatureMatches: SignatureMatchMap = mapOf(),
    val values: Map<PathKey, Any> = mapOf()
)
