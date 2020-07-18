package silentorb.imp.parsing.resolution

import silentorb.imp.core.NodeMap
import silentorb.imp.core.PathKey
import silentorb.imp.core.TypeHash
import silentorb.imp.parsing.syntax.Burg
import silentorb.imp.parsing.syntax.BurgId

typealias TokenIndex = Int

typealias TokenParents = Map<BurgId, List<BurgId>>

data class FunctionApplication(
    val target: PathKey,
    val arguments: List<PathKey>
)

data class IntermediateExpression(
    val applications: Map<PathKey, FunctionApplication>,
    val literalTypes: Map<PathKey, TypeHash>,
    val namedArguments: Map<PathKey, Burg>,
    val nodeMap: NodeMap,
    val parents: Map<PathKey, List<PathKey>>,
    val references: Map<PathKey, String>,
    val stages: List<PathKey>,
    val values: Map<PathKey, Any>
)
