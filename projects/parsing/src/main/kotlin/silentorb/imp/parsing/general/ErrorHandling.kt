package silentorb.imp.parsing.general

import silentorb.imp.core.DependencyError
import silentorb.imp.core.FileRange
import silentorb.imp.core.ImpError
import silentorb.imp.parsing.syntax.Burg

fun newParsingError(message: TextId, token: Token, arguments: List<Any> = listOf()) =
    ImpError(
        message = message,
        fileRange = token.fileRange,
        arguments = arguments
    )

fun newParsingError(message: TextId): (Token) -> ImpError = { token ->
  ImpError(
      message = message,
      fileRange = token.fileRange
  )
}

fun newParsingError(message: TextId, fileRange: FileRange) =
    ImpError(
        message = message,
        fileRange = fileRange
    )

fun newParsingError(message: TextId, burg: Burg) =
    ImpError(
        message = message,
        fileRange = burg.fileRange
    )

fun newParsingError(burg: Burg): (DependencyError) -> ImpError = { error ->
  newParsingError(TextId.circularDependency, burg)
}
