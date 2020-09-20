package silentorb.imp.parsing.syntaxNew

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.syntax.BurgType

data class NestedBurg(
    val type: BurgType,
    val range: Range,
    val children: List<NestedBurg> = listOf(),
    val value: Any? = null
)

fun newNestedBurg(type: BurgType, token: Token, children: List<NestedBurg> = listOf(), value: Any? = null) =
    NestedBurg(
        type = type,
        range = token.range,
        children = children,
        value = value
    )

fun newNestedBurg(type: BurgType, children: List<NestedBurg>): NestedBurg {
  return if (children.none())
    NestedBurg(
        type = type,
        range = Range(newPosition(), newPosition())
    )
  else
    NestedBurg(
        type = type,
        range = Range(
            children.minByOrNull { it.range.start.index }!!.range.start,
            children.maxByOrNull { it.range.end.index }!!.range.end
        ),
        children = children
    )
}

data class ParsingResponse(
    val tokens: Tokens,
    val burgs: List<NestedBurg> = listOf(),
    val errors: ImpErrors = listOf(),
    val exitLoop: Boolean = false
) {
  operator fun plus(other: ParsingResponse) =
      ParsingResponse(
          other.tokens,
          this.burgs + other.burgs,
          this.errors + other.errors,
          this.exitLoop || other.exitLoop
      )
}

typealias ParsingFunction = (Tokens) -> ParsingResponse
typealias Router = (Token) -> ParsingFunction
typealias OptionalRouter = (Token) -> ParsingFunction?
