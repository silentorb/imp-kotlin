package silentorb.imp.parsing.general

sealed class Response<out T> {
  data class Success<T>(val value: T) : Response<T>()
  data class Failure<T>(val errors: List<LexicalError>) : Response<T>()
}

sealed class DurableResponse<out T> {
  data class Success<T>(val value: T) : DurableResponse<T>()
  data class Failure<T>(val errors: List<LexicalError>, val value: T) : DurableResponse<T>()
}

fun <T> failure(errors: List<LexicalError>): Response<T> =
    Response.Failure(errors)

fun <T> success(value: T): Response<T> =
    Response.Success(value)

fun <I, T> handleDurable(onFailure: (I) -> T, response: DurableResponse<I>, onSuccess: (I) -> DurableResponse<T>): DurableResponse<T> =
    when (response) {
      is DurableResponse.Success -> onSuccess(response.value)
      is DurableResponse.Failure -> DurableResponse.Failure(response.errors, onFailure(response.value))
    }

fun <I> handleRoot(onFailure: (List<LexicalError>) -> Unit, response: Response<I>, onSuccess: (I) -> Unit) {
  when (response) {
    is Response.Success -> onSuccess(response.value)
    is Response.Failure -> onFailure(response.errors)
  }
}

fun <I> expectErrors(onSuccess: () -> Unit, response: Response<I>, onFailure: (List<LexicalError>) -> Unit) {
  when (response) {
    is Response.Success -> onSuccess()
    is Response.Failure -> onFailure(response.errors)
  }
}

fun <I, T> handle(response: Response<I>, onSuccess: (I) -> Response<T>): Response<T> =
    when (response) {
      is Response.Success -> onSuccess(response.value)
      is Response.Failure -> failure(response.errors)
    }

fun <I, O> nullify(function: () -> O): (I) -> O = { _ ->
  function()
}
