package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.Id
import silentorb.imp.core.NextId

typealias NodeTokenMap = Map<Id, TokenIndex>

fun tokensToNodes(nextId: NextId, nodeReferences: NodeReferenceMap, indexes: List<TokenIndex>): Map<Id, TokenIndex> {
  return indexes.associateBy { tokenIndex ->
    val nodeReference = nodeReferences[tokenIndex]
    nodeReference?.first ?: nextId()
  }
}
