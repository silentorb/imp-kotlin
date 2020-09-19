package silentorb.imp.parsing.syntaxNew

import silentorb.imp.core.ImpError
import silentorb.imp.core.ImpErrors
import silentorb.imp.core.Range
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.syntax.BurgType
import silentorb.imp.parsing.syntax.ValueTranslator
import silentorb.imp.parsing.syntax.asString

//operator fun ParsingFunctionTransform.plus(other: ParsingFunction): ParsingFunction =
//    this(other)

//operator fun ParsingFunction.plus(other: ParsingFunction): ParsingFunction =
//    this(other)

operator fun ParsingFunction.plus(other: ParsingFunction): ParsingFunction = { tokens ->
  val first = this(tokens)
  val second = other(first.first)
  ParsingResponse(
      second.first,
      first.second + second.second,
      first.third + second.third
  )
}

tailrec fun reduceParsers(
    functions: Collection<ParsingFunction>,
    tokens: Tokens,
    burgs: List<NestedBurg> = listOf(),
    errors: ImpErrors = listOf()
): ParsingResponse =
    if (functions.none())
      Triple(tokens, burgs, errors)
    else {
      val next = functions.first()
      val step = next(tokens)
      val (nextTokens, newBurgs, newErrors) = step
      reduceParsers(functions.drop(1), nextTokens, burgs + newBurgs, errors + newErrors)
    }

fun wrapResponseList(burgType: BurgType, tokens: Tokens, response: ParsingResponse): ParsingResponse {
  val (nextTokens, children, errors) = response
  val token = tokens.first()
  val end = children.maxByOrNull { it.range.end.index }?.range?.end ?: token.range.end
  return ParsingResponse(
      nextTokens,
      listOf(NestedBurg(
          type = burgType,
          file = token.file,
          children = children,
          range = Range(token.range.start, end)
      )),
      errors
  )
}

fun wrap(burgType: BurgType, vararg functions: ParsingFunction): ParsingFunction = { tokens ->
  val response = reduceParsers(functions.toList(), tokens)
  wrapResponseList(burgType, tokens, response)
}

fun wrap(burgType: BurgType): ParsingFunctionTransform = { function ->
  { tokens ->
    val (nextTokens, children, errors) = function(tokens)
    val token = tokens.first()
    ParsingResponse(
        nextTokens,
        listOf(NestedBurg(
            type = burgType,
            file = token.file,
            children = children,
            range = Range(token.range.start, children.lastOrNull()?.range?.end ?: token.range.end)
        )),
        errors
    )
  }
}

fun success(tokens: Tokens, burg: NestedBurg?) = ParsingResponse(tokens, listOfNotNull(burg), listOf())

val consume: ParsingFunction = { tokens ->
  success(tokens.drop(1), null)
}

fun addError(message: TextId): ParsingFunction {
  return { tokens ->
    ParsingResponse(tokens, listOf(), listOf(ImpError(message, tokens.firstOrNull()?.fileRange)))
  }
}

fun route(tokens: Tokens, router: (Token) -> ParsingFunction): ParsingResponse =
    if (tokens.none())
      ParsingResponse(tokens,  listOf(), listOf(ImpError(TextId.expectedExpression)))
    else
      router(tokens.first())(tokens)

fun route(router: (Token) -> ParsingFunction): ParsingFunction = { tokens ->
  route(tokens, router)
}

fun consumeToken(burgType: BurgType, translator: ValueTranslator = asString): ParsingFunction = { tokens ->
  val token = tokens.first()
  success(tokens.drop(1), newNestedBurg(burgType, token, value = translator(token.value)))
}

fun consumeExpected(condition: (Token) -> Boolean, errorMessage: TextId, function: ParsingFunction = consume): ParsingFunction = { tokens ->
  val token = tokens.first()
  if (condition(token))
    function(tokens)
  else
    ParsingResponse(tokens,  listOf(), listOf(ImpError(errorMessage, tokens.firstOrNull()?.fileRange)))
}
