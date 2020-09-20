package silentorb.imp.parsing.structureOld

import silentorb.imp.core.PathKey
import silentorb.imp.parsing.resolution.getLiteralBurgType
import silentorb.imp.parsing.syntax.Burg
import silentorb.imp.parsing.syntax.BurgId

fun literalTokenNodes(path: String, burgs: Collection<Burg>): Map<Burg, PathKey> {
  val literalNodes = burgs.filter { getLiteralBurgType(it.type) != null }
  return literalNodes
      .mapIndexed { index, tokenIndex -> Pair(tokenIndex, PathKey(path, "#literal${index + 1}")) }
      .associate { it }
}
