package silentorb.imp.execution

import silentorb.imp.core.*

fun compileNodeFunction(context: Context, signature: Signature, key: PathKey, functionGraph: Namespace): FunctionImplementation {
  val parameters = signature.parameters
  val inlinedValues = inlineValues(context, functionGraph, parameters.map { it.name })
  return { arguments: Arguments ->
    val values = inlinedValues + parameters.associate {
      Pair(PathKey(pathKeyToString(key), it.name), arguments[it.name]!!)
    }
    val newContext = newNamespace().copy(
        values = values
    )
    executeToSingleValue(context + newContext, functionGraph)!!
  }
}

fun getImplementationFunctions(context: Context, implementationGraphs: Map<PathKey, Namespace>): FunctionImplementationMap {
  return implementationGraphs.mapValues { (key, functionGraph) ->
    val signature = getTypeSignature(context, key.type!!)!!
    compileNodeFunction(context, signature, key, functionGraph)
  }
}
