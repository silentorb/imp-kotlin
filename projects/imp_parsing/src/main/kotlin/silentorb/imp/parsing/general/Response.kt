package silentorb.imp.parsing.general

sealed class Response<out T> {
  data class Success<T>(val value: T) : Response<T>()
  data class Failure<T>(val errors: List<ParsingError>) : Response<T>()

  fun <O> map(transform: (T) -> O): Response<O> =
      handle(this) { success(transform(it)) }

  fun <O> then(transform: (T) -> Response<O>): Response<O> =
      handle(this) { transform(it) }
}

fun <T> failure(errors: List<ParsingError>): Response<T> =
    Response.Failure(errors)

fun <T> failure(error: ParsingError): Response<T> =
    Response.Failure(listOf(error))

fun <T> success(value: T): Response<T> =
    Response.Success(value)

fun <I> handleRoot(onFailure: (List<ParsingError>) -> Unit, response: Response<I>, onSuccess: (I) -> Unit) {
  when (response) {
    is Response.Success -> onSuccess(response.value)
    is Response.Failure -> onFailure(response.errors)
  }
}

fun <I, T> handle(response: Response<I>, onSuccess: (I) -> Response<T>): Response<T> =
    when (response) {
      is Response.Success -> onSuccess(response.value)
      is Response.Failure -> failure(response.errors)
    }

fun <I> flatten(responses: List<Response<I>>): Response<List<I>> {
  val failed = responses
      .filterIsInstance<Response.Failure<I>>()

  return if (failed.any())
    failure(failed.flatMap { it.errors })
  else
    success((responses as List<Response.Success<I>>).map { it.value })
}

fun <T> checkForErrors(check: (T) -> List<ParsingError>): (T) -> Response<T> = { subject ->
  val errors = check(subject)
  if (errors.any())
    failure(errors)
  else
    success(subject)
}
