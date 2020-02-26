package silentorb.imp.parsing.general

import silentorb.imp.core.Signature

sealed class Response<T> {
  data class Success<T>(val value: T) : Response<T>()
  data class Failure<T>(val errors: List<ParsingError>) : Response<T>()

  fun <O> map(transform: (T) -> O): Response<O> =
      handle(this) { success(transform(it)) }

  fun <O> then(transform: (T) -> Response<O>): Response<O> =
      handle(this) { transform(it) }

  fun done(onFailure: (List<ParsingError>) -> Unit, onSuccess: (T) -> Unit) {
    handleRoot(onFailure, this, onSuccess)
  }

  fun onError(onFailure: (List<ParsingError>) -> T): T {
    return when (this) {
      is Success -> this.value
      is Failure -> onFailure(this.errors)
    }
  }

  fun throwOnFailure(onFailure: (List<ParsingError>) -> Error): T {
    return when (this) {
      is Success -> this.value
      is Failure -> throw onFailure(this.errors)
    }
  }
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

typealias PartitionedResponse<I> = Pair<I, ParsingErrors>

fun <I, O> partition(responses: Collection<Response<I>>, transform: (List<I>) -> O): PartitionedResponse<O> {
  val (successes, failures) = responses
      .partition { it is Response.Success }

  return Pair(
      transform((successes as List<Response.Success<I>>).map { it.value }),
      failures.flatMap { (it as Response.Failure).errors }
  )
}

fun <K, V> partitionMap(responses: Map<K, Response<V>>): PartitionedResponse<Map<K, V>> {
  return Pair(
      responses.filterValues { it is Response.Success }.mapValues { (it.value as Response.Success<V>).value },
      responses.filterValues { it is Response.Failure }.flatMap { (it.value as Response.Failure<V>).errors }
  )
}

fun <T> checkForErrors(check: (T) -> List<ParsingError>): (T) -> Response<T> = { subject ->
  val errors = check(subject)
  if (errors.any())
    failure(errors)
  else
    success(subject)
}
