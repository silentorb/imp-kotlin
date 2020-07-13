package silentorb.imp.execution

import silentorb.imp.core.*

fun getImplementationFunctions(context: Context, implementationGraphs: Map<FunctionKey, Namespace>, functions: () -> FunctionImplementationMap): FunctionImplementationMap {
  return implementationGraphs.mapValues { (key, functionGraph) ->
    val signature = getTypeSignature(context, key.type)!!
    val parameters = signature.parameters
    val inlinedValues = inlineValues(context, functionGraph, parameters.map { it.name })
    val notUsed = 1
    { arguments: Arguments ->
      val values = inlinedValues + parameters.associate {
        Pair(PathKey(pathKeyToString(key.key), it.name), arguments[it.name]!!)
      }
      val newContext = newNamespace().copy(
          values = values
      )
      executeToSingleValue(context + newContext, functionGraph)!!
    }
  }
}
