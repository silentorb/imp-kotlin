package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.OverloadsMap

fun partitionFunctions(functions: List<CompleteFunction>): Pair<OverloadsMap, FunctionImplementationMap> {
  val interfaces = functions
      .associate {
        Pair(it.path, mapOf(
            it.signature to it.parameters
        ))
      }
  val implementations = functions
      .associate {
        Pair(FunctionKey(it.path, it.signature), it.implementation)
      }
  return Pair(interfaces, implementations)
}
