package silentorb.imp.core

data class ImpError(
    val message: Any,
    val fileRange: FileRange? = null,
    val arguments: List<Any> = listOf()
)

typealias ImpErrors = List<ImpError>

fun formatError(textLibrary: (Any) -> String, error: ImpError): String {
  val rangeClause = if (error.fileRange != null)
    " at ${rangeString(error.fileRange)}"
  else
    ""

  val initial = textLibrary(error.message) + rangeClause
  return String.format(initial, *error.arguments.toTypedArray())
}

data class Response<T>(
    val value: T,
    val errors: ImpErrors
)

fun <T> flattenResponses(responses: List<Response<T>>): Response<List<T>> =
    Response(
        responses.map { it.value },
        responses.flatMap { it.errors }
    )
