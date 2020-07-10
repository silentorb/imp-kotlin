import org.junit.jupiter.api.Assertions
import silentorb.imp.core.Dungeon
import silentorb.imp.core.ImpError
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.TextId

fun <I> handleRoot(onFailure: (List<ImpError>) -> Unit, response: Response<I>, onSuccess: (I) -> Unit) {
  if (response.errors.none())
    onSuccess(response.value)
  else
    onFailure(response.errors)
}

fun assertUnknownFunctionError(response: Response<Dungeon>) {
  val (_, errors) = response
  Assertions.assertEquals(1, errors.size)
  Assertions.assertEquals(TextId.unknownFunction, errors.first().message)
}
