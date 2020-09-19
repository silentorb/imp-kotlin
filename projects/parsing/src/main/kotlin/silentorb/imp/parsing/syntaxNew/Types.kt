package silentorb.imp.parsing.syntaxNew

import silentorb.imp.core.FileRange
import silentorb.imp.core.ImpErrors
import silentorb.imp.core.Range
import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.syntax.BurgType

data class NestedBurg(
    val type: BurgType,
    val range: Range,
    val file: TokenFile,
    val children: List<NestedBurg> = listOf(),
    val value: Any? = null
) {
  val fileRange: FileRange get() = FileRange(file, range)

  init {
    if (file != range.start.file) {
      val k = 0
    }
  }
}

fun newNestedBurg(type: BurgType, token: Token, children: List<NestedBurg> = listOf(), value: Any? = null) =
    NestedBurg(
        type = type,
        range = token.range,
        file = token.file,
        children = children,
        value = value
    )

typealias ParsingResponse = Triple<Tokens, List<NestedBurg>, ImpErrors>
//typealias ParsingListResponse = Triple<Tokens, List<NestedBurg>, ImpErrors>
typealias ParsingFunction = (Tokens) -> ParsingResponse
typealias ParsingFunctionTransform = (ParsingFunction) -> ParsingFunction
//typealias ParsingListFunction = (Tokens) -> ParsingListResponse
