package silentorb.imp.core

tailrec fun getRootType(context: Context, type: TypeHash): TypeHash {
  val alias = getTypeAlias(context, type)
  return if (alias == null)
    type
  else
    getRootType(context, alias)
}

fun typeCanBeCastTo(context: Context, source: TypeHash, target: TypeHash): Boolean {
  return getRootType(context, source) == getRootType(context, target)
}

fun overloadMatches(context: Context, arguments: List<Argument>, overloads: List<Signature>): List<SignatureMatch> {
  val argumentsByType = arguments.groupBy { it.type }
  val matches = overloads
      .mapNotNull { signature ->
        val namedArguments = arguments
            .filter { argument -> argument.name != null }
            .mapNotNull { argument ->
              val parameter = signature.parameters.firstOrNull { it.name == argument.name!! }
              if (parameter != null && typeCanBeCastTo(context, argument.type, parameter.type))
                Pair(parameter.name, argument.node)
              else
                null
            }
        val indexedArguments = argumentsByType
            .flatMap { (argumentType, typeArguments) ->
              val parameters = signature.parameters.filter { parameter ->
                typeCanBeCastTo(context, argumentType, parameter.type) && namedArguments.none { it.first == parameter.name }
              }
              if (parameters.size == typeArguments.size)
                typeArguments.zip(parameters) { a, b -> Pair(b.name, a.node) }
              else
                listOf()
            }
        val alignment = namedArguments.plus(indexedArguments)
        if (alignment.size < arguments.size)
          null
        else
          SignatureMatch(
              signature = signature,
              alignment = alignment.associate { it },
              complete = signature.parameters.size == arguments.size
          )
      }
  if (matches.none()) {
    val k = 0
  }
  return matches
}

tailrec fun resolveReferenceValue(context: Context, key: PathKey): Any? {
  val value = getValue(context, key)
  return if (value != null)
    value
  else {
    val connections = getInputConnections(context, key)
    val isSingleConnection = connections.size == 1 && connections.keys.first().parameter == defaultParameter
    if (!isSingleConnection) {
      null
    } else {
      val reference = connections.values.first()
      resolveReferenceValue(context, reference)
    }
  }
}
