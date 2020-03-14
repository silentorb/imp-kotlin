package silentorb.imp.core

data class Graph(
    val nodes: Set<Id> = setOf(),
    val connections: Set<Connection> = setOf(),
    val functionTypes: Map<Id, PathKey> = mapOf(),
    val types: Map<Id, PathKey> = mapOf(),
    val signatureMatches: SignatureMatchMap = mapOf(),
    val values: Map<Id, Any> = mapOf()
)
