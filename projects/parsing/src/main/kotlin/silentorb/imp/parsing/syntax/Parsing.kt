package silentorb.imp.parsing.syntax

import silentorb.imp.core.*
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.syntax.traversing.*
import silentorb.imp.parsing.syntaxNew.NestedBurg
import silentorb.mythic.debugging.getDebugBoolean

fun getTransition(token: Token, mode: ParsingMode, contextMode: ContextMode): ParsingStep {
  val contextAction: ContextualTokenToParsingTransition? =
      when (mode) {
        ParsingMode.expressionArgumentStart -> parseExpressionArgumentStart
        ParsingMode.expressionArgumentFollowing -> parseExpressionFollowingArgument
        ParsingMode.pipingRootStart -> parsePipingRootStart
        else -> null
      }

  if (contextAction != null)
    return contextAction(token, contextMode)

  val simpleAction: TokenToParsingTransition =
      when (mode) {
        ParsingMode.block -> parseBody
        ParsingMode.definitionAssignment -> parseDefinitionAssignment
        ParsingMode.definitionBodyStart -> parseDefinitionBodyStart
        ParsingMode.definitionParameterColon -> parseDefinitionParameterColon
        ParsingMode.definitionParameterNameOrAssignment -> parseDefinitionParameterNameOrAssignment
        ParsingMode.definitionParameterType -> parseDefinitionParameterType
        ParsingMode.definitionName -> parseDefinitionName
        ParsingMode.expressionNamedArgumentValue -> parseExpressionNamedArgumentValue
        ParsingMode.expressionStart -> parseExpressionStart
        ParsingMode.header -> parseHeader
        ParsingMode.importFirstPathToken -> parseImportFirstPathToken
        ParsingMode.importFollowingPathToken -> parseImportFollowingPathToken
        ParsingMode.importSeparator -> parseImportSeparator
        else -> throw Error()
      }

  return simpleAction(token)
}

fun newBurg(file: TokenFile, token: Token): NewBurg = { burgType, valueTranslator ->
  Burg(
      type = burgType,
      range = token.range,
      children = listOf(),
      value = valueTranslator(token.value)
  )
}

fun logTransition(token: Token, previousState: ParsingState, nextState: ParsingState) {
  val value = if (token.value.isEmpty())
    token.rune.name
  else
    token.value

  val burgStack = nextState.burgStack.map { it.first().type.name }.joinToString(", ").padEnd(100)
  println("[$burgStack] ${(value).padStart(12)} ${previousState.mode.name} -> ${nextState.mode.name}")
}

tailrec fun parsingStep(
    file: TokenFile,
    tokens: Tokens,
    state: ParsingState
): ParsingState =
    if (tokens.none())
      state
    else {
      val token = tokens.first()
      val contextMode = state.contextStack.lastOrNull() ?: ContextMode.root
      val transition = getTransition(token, state.mode, contextMode)
      val nextState = transition(newBurg(file, token), state)
      val nextTokens = tokens.drop(1)

      if (getDebugBoolean("IMP_PARSING_LOG_TRANSITIONS")) {
        logTransition(token, state, nextState)
      }
      parsingStep(file, nextTokens, nextState)
    }

fun toBurg(file: TokenFile, nestedBurg: NestedBurg): Burg =
    Burg(
        type = nestedBurg.type,
        range = nestedBurg.range, // Eventually refactor this to use a accumulator and tailrec
        children = nestedBurg.children.map { toBurg(file, it) },
        value = nestedBurg.value
    )

fun flattenNestedBurg(burg: Burg): Set<Burg> =
    setOf(burg) +
        burg.children
            .flatMap(::flattenNestedBurg)

//fun parseSyntaxOld(file: TokenFile, tokens: Tokens): Response<Realm> {
//  val sanitizedTokens = if (tokens.size == 0 || tokens.last().rune != Rune.newline)
//    tokens + Token(Rune.newline, FileRange("", Range(newPosition(), newPosition())), "")
//  else
//    tokens
//  val closedTokens = sanitizedTokens + Token(Rune.eof, emptyFileRange(), "")
//  val state = fold(parsingStep(file, closedTokens, newState(file, ParsingMode.header)))
//
//  assert(state.burgStack.size == 1)
//
//  val root = state.burgStack.first().first()
//  val realm = Realm(
//      root = root.hashCode(),
//      burgs = state.accumulator
//          .plus(root)
//          .associateBy { it.hashCode() }
//  )
//
//  if (getDebugBoolean("IMP_PARSING_LOG_HIERARCHY"))
//    logRealmHierarchy(realm)
//
//  val convertedErrors = state.errors.map { error ->
//    ImpError(
//        message = error.message,
//        fileRange = FileRange(file, error.range)
//    )
//  }
//
//  val letErrors = tokens
//      .filterIndexed { index, token ->
//        isLet(token) && index > 0 && !(isNewline(tokens[index - 1]) || isBraceOpen(tokens[index - 1]))
//      }
//      .map { newParsingError(TextId.expectedNewline, it) }
//
//  return Response(
//      realm,
//      convertedErrors + letErrors
//  )
//}

fun parseSyntax(file: TokenFile, tokens: Tokens): Response<Realm?> {
  val sanitizedTokens = if (tokens.size == 0 || tokens.last().rune != Rune.newline)
    tokens + Token(Rune.newline, FileRange("", Range(newPosition(), newPosition())), "")
  else
    tokens

  val closedTokens = sanitizedTokens + Token(Rune.eof, emptyFileRange(), "")
//  val (_, nestedBurgs, errors) = parseTokens(closedTokens)
  val state = fold(parsingStep(file, closedTokens, newState(file, ParsingMode.header)))
  val rootLayer = state.burgStack.firstOrNull()
  val root = rootLayer?.firstOrNull()
  assert(state.burgStack.size < 2)
  assert(rootLayer == null || rootLayer.size < 2)
  val realm = if (root != null) {
    val burgs = flattenNestedBurg(root)
    Realm(root, burgs)
  } else
    null

  val errors = state.errors.map { error ->
    ImpError(
        message = error.message,
        fileRange = FileRange(file, error.range)
    )
  }
  val letErrors = tokens
      .filterIndexed { index, token ->
        isLet(token) && index > 0 && !(isNewline(tokens[index - 1]) || isBraceOpen(tokens[index - 1]))
      }
      .map { newParsingError(TextId.expectedNewline, it) }

  return Response(
      realm,
      errors + letErrors
  )
}
