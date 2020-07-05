import silentorb.imp.core.ImpError
import silentorb.imp.core.Response

fun <I> handleRoot(onFailure: (List<ImpError>) -> Unit, response: Response<I>, onSuccess: (I) -> Unit) {
  if (response.errors.none())
    onSuccess(response.value)
  else
    onFailure(response.errors)
}
