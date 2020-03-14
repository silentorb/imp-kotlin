package silentorb.imp.core

tailrec fun getRootType(aliases: Aliases, type: PathKey): PathKey {
  val alias = aliases[type]
  return if (alias == null)
    type
  else
    getRootType(aliases, alias)
}

fun typeCanBeCastTo(aliases: Aliases, source: PathKey, target: PathKey): Boolean {
  return getRootType(aliases, source) == getRootType(aliases, target)
}

fun overloadMatches(aliases: Aliases, arguments: List<Argument>, overloads: Signatures): List<SignatureMatch> {
  // TODO: Move this to a better area lower in the stack as a more formal integrity check
  if (arguments.any { it.name == null } && arguments.any { it.name != null })
    return listOf()

  val argumentsByType = arguments.groupBy { it.type }

  return overloads
      .mapNotNull { signature ->
        val alignment = if (arguments.any { it.name == null }) {
          argumentsByType.flatMap { (argumentType, typeArguments) ->
            val parameters = signature.parameters.filter { typeCanBeCastTo(aliases, argumentType, it.type) }
            if (parameters.size == typeArguments.size)
              typeArguments.zip(parameters) { a, b -> Pair(b.name, a.node) }
            else
              listOf()
          }
        } else {
          arguments.mapNotNull { argument ->
            val parameter = signature.parameters.firstOrNull { it.name == argument.name!! }
            if (parameter != null && typeCanBeCastTo(aliases, argument.type, parameter.type))
              Pair(parameter.name, argument.node)
            else
              null
          }
        }
        if (alignment.size < arguments.size)
          null
        else
          SignatureMatch(signature, alignment.associate { it })
      }
}
