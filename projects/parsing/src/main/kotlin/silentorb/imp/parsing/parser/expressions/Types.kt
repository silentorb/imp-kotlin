package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.NodeMap
import silentorb.imp.core.PathKey
import silentorb.imp.core.TypeHash
import silentorb.imp.parsing.syntax.Burg
import silentorb.imp.parsing.syntax.BurgId
import silentorb.imp.parsing.syntax.Realm

typealias TokenIndex = Int

typealias TokenParents = Map<BurgId, List<BurgId>>

data class TokenGraph(
    val burgs: Map<BurgId, Burg>,
    val parents: TokenParents,
    val stages: List<List<BurgId>>
)

data class IntermediateExpression(
    val literalTypes: Map<PathKey, TypeHash>,
    val namedArguments: Map<PathKey, Burg>,
    val nodeMap: NodeMap,
    val parents: Map<PathKey, List<PathKey>>,
    val references: Map<String, Set<PathKey>>,
    val stages: List<PathKey>,
    val values: Map<PathKey, Any>
)
