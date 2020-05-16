package silentorb.imp.core

//tailrec fun getRootType(context: Context, type: TypeHash): TypeHash? {
//  else if (alias.size == 1)
//    getRootType(context, alias.first().key)
//  else
//    alias
//}

fun typeCanBeCastTo(context: Context, source: TypeHash, target: TypeHash): Boolean {
  return source == target
//  return getRootType(context, source) == getRootType(context, target)
}

fun overloadMatches(context: Context, arguments: List<Argument>, overloads: List<Signature>): List<SignatureMatch> {
  val argumentsByType = arguments.groupBy { it.type }
  return overloads
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
          SignatureMatch(signature, alignment.associate { it })
      }
}
