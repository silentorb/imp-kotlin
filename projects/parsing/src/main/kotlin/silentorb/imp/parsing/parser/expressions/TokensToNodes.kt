package silentorb.imp.parsing.parser.expressions

import silentorb.imp.core.Id
import silentorb.imp.core.NextId

typealias NodeTokenMap = Map<Id, TokenIndex>

fun tokensToNodes(nextId: NextId, nodeReferences: NodeReferenceMap, indexes: List<TokenIndex>): Map<TokenIndex, Id> {
  return indexes.associateWith { tokenIndex ->
    val nodeReference = nodeReferences[tokenIndex]
    nodeReference?.first ?: nextId()
  }
}
