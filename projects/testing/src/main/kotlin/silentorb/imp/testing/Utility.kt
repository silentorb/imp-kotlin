package silentorb.imp.testing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.rangeString

val errored = { errors: List<ParsingError> ->
  val error = errors.firstOrNull()
  val message = if (error != null) {
    "${error.message} at (${rangeString(error.range)})"
  } else
    ""
  assertEquals(message, 0, errors.size)
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
