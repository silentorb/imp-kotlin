package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.NodeMap
import silentorb.imp.core.PathKey
import silentorb.imp.core.TypeHash

typealias TokenIndex = Int

typealias TokenParents = Map<TokenIndex, List<TokenIndex>>

data class TokenGraph(
    val parents: TokenParents,
    val stages: List<List<TokenIndex>>
)

data class IntermediateExpression(
    val literalTypes: Map<PathKey, TypeHash>,
    val namedArguments: Map<PathKey, String>,
    val nodeMap: NodeMap,
    val parents: Map<PathKey, List<PathKey>>,
    val references: Map<String, Set<PathKey>>,
    val stages: List<List<PathKey>>,
    val values: Map<PathKey, Any>
)
