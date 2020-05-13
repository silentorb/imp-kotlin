package silentorb.imp.core

tailrec fun getRootType(aliases: Aliases, type: PathKey): PathKey {
  val alias = aliases[type]
  return if (alias == null)
    type
  else
    getRootType(aliases, alias)
}

tailrec fun getRootType(context: Context, type: PathKey): PathKey {
  val alias = resolveAlias(context, type)
  return if (alias == type)
    type
  else
    getRootType(context, alias)
}

fun typeCanBeCastTo(aliases: Aliases, source: PathKey, target: PathKey): Boolean {
  return getRootType(aliases, source) == getRootType(aliases, target)
}

fun overloadMatches(aliases: Aliases, arguments: List<Argument>, overloads: Signatures): List<SignatureMatch> {
  val argumentsByType = arguments.groupBy { it.type }

  return overloads
      .mapNotNull { signature ->
        val namedArguments = arguments
            .filter { argument -> argument.name != null }
            .mapNotNull { argument ->
              val parameter = signature.parameters.firstOrNull { it.name == argument.name!! }
              if (parameter != null && typeCanBeCastTo(aliases, argument.type, parameter.type))
                Pair(parameter.name, argument.node)
              else
                null
            }
        val indexedArguments = argumentsByType
            .flatMap { (argumentType, typeArguments) ->
              val parameters = signature.parameters.filter { parameter ->
                typeCanBeCastTo(aliases, argumentType, parameter.type) && namedArguments.none { it.first == parameter.name }
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
