package silentorb.imp.parsing.general

import silentorb.imp.core.FileRange
import silentorb.imp.core.Range
import silentorb.imp.core.rangeString

data class ParsingError(
    val message: Any,
    val fileRange: FileRange,
    val arguments: List<Any> = listOf()
)

typealias ParsingErrors = List<ParsingError>

fun newParsingError(message: TextId, fileRange: FileRange) =
    ParsingError(
        message = message,
        fileRange = fileRange
    )

fun errorIf(condition: Boolean, message: TextId, fileRange: FileRange): ParsingError? =
    if (condition)
      ParsingError(message, fileRange = fileRange)
    else
      null

fun formatError(textLibrary: (Any) -> String, error: ParsingError): String {
  val initial = textLibrary(error.message) + " at ${rangeString(error.fileRange)}"
  return String.format(initial, *error.arguments.toTypedArray())
}

data class ParsingResponse<T>(
    val value: T,
    val errors: ParsingErrors
)

fun <T> flattenResponses(responses: List<ParsingResponse<T>>): ParsingResponse<List<T>> =
    ParsingResponse(
        responses.map { it.value },
        responses.flatMap { it.errors }
    )

fun <K, V> flattenResponseMap(responses: Map<K, ParsingResponse<V>>): ParsingResponse<Map<K, V>> =
    ParsingResponse(
        responses.mapValues { it.value.value },
        responses.flatMap { it.value.errors }
    )
