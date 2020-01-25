import org.junit.Assert.*
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.TextId

val errored = { errors: List<ParsingError> ->
  // This line should not be hit
  assertEquals(errors.firstOrNull()?.message?.toString() ?: "", 0, errors.size)
}

val shouldHaveErrored = { ->
  // This line should not be hit
  assertTrue(false)
}

fun <I> expectErrors(onSuccess: () -> Unit, response: Response<I>, onFailure: (List<ParsingError>) -> Unit) {
  when (response) {
    is Response.Success -> onSuccess()
    is Response.Failure -> onFailure(response.errors)
  }
}

fun <I> expectError(textId: TextId, response: Response<I>) =
    expectErrors(shouldHaveErrored, response) { errors ->
      if (errors.any { it.message == textId })
        assertTrue(true)
      else
        assertEquals(textId, errors.firstOrNull()?.message)
    }
