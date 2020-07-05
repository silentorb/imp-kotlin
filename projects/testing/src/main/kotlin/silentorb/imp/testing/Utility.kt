package silentorb.imp.testing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import silentorb.imp.core.ImpError
import silentorb.imp.core.Response
import silentorb.imp.core.formatError
import silentorb.imp.parsing.general.*

val errored = { errors: List<ImpError> ->
  val error = errors.firstOrNull()
  val message = if (error != null) {
    "[TextId.${error.message}] ${formatError(::englishText, error)}"
  } else
    ""
  assertEquals(0, errors.size, message)
}

val shouldHaveErrored = { ->
  // This line should not be hit
  assertTrue(false)
}

fun <I> expectErrors(onSuccess: () -> Unit, response: Response<I>, onFailure: (List<ImpError>) -> Unit) {
  if (response.errors.any())
    onFailure(response.errors)
  else
    onSuccess()
}

fun <I> expectError(textId: TextId, response: Response<I>) =
    expectErrors(shouldHaveErrored, response) { errors ->
      if (errors.any { it.message == textId }) {
        formatError(::englishText, errors.first())
        assertTrue(true)
      } else
        assertEquals(textId, errors.firstOrNull()?.message)
    }
