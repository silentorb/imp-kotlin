package silentorb.imp.parsing.general

import silentorb.imp.core.Range
import silentorb.imp.core.rangeString

data class ParsingError(
    val message: TextId,
    val range: Range,
    val arguments: List<Any> = listOf()
)

typealias ParsingErrors = List<ParsingError>

fun newParsingError(message: TextId, range: Range) =
    ParsingError(
        message = message,
        range = range
    )

fun errorIf(condition: Boolean, message: TextId, range: Range): ParsingError? =
    if (condition)
      ParsingError(message, range = range)
    else
      null

fun formatError(textLibrary: (TextId) -> String, error: ParsingError): String {
  val initial = textLibrary(error.message) + " at ${rangeString(error.range)}"
  return String.format(initial, *error.arguments.toTypedArray())
}

data class PartitionedResponse<T>(
    val value: T,
    val errors: ParsingErrors
)

fun <T> flattenResponses(responses: List<PartitionedResponse<T>>): PartitionedResponse<List<T>> =
    PartitionedResponse(
        responses.map { it.value },
        responses.flatMap { it.errors }
    )
