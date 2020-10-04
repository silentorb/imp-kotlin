import org.junit.jupiter.api.Assertions
import silentorb.imp.core.Context
import silentorb.imp.core.Dungeon
import silentorb.imp.core.ImpError
import silentorb.imp.core.Response
import silentorb.imp.parsing.general.CodeBuffer
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.parser.parseToDungeon
import silentorb.imp.testing.errored

fun <I> handleRoot(onFailure: (List<ImpError>) -> Unit, response: Response<I>, onSuccess: (I) -> Unit) {
  if (response.errors.none())
    onSuccess(response.value)
  else
    onFailure(response.errors)
}

fun parseWithThrow(context: Context, code: CodeBuffer): Dungeon {
  val response = parseToDungeon(context, code)
  if (response.errors.none())
    return response.value
  else {
    errored(response.errors)
    throw Error("This shouldn't be hit.  An error should already be thrown.  This line exists to let the compiler know.")
  }
}

fun assertUnknownFunctionError(response: Response<Dungeon>) {
  val (_, errors) = response
  Assertions.assertEquals(1, errors.size)
  Assertions.assertEquals(TextId.unknownFunction, errors.first().message)
}
