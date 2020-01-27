import org.junit.Assert.*
import silentorb.imp.core.Context
import silentorb.imp.core.Namespace
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

fun addNamespaceFunction(namespace: Namespace, path: List<String>, value: String): Namespace {
  val name = path.first()
  return if (path.size == 1)
    namespace.copy(
        functions = namespace.functions.plus(Pair(name, value))
    )
  else {
    val child = namespace.namespaces[name] ?: Namespace()
    val newChild = addNamespaceFunction(child, path.drop(1), value)
    namespace.copy(
        namespaces = namespace.namespaces.plus(Pair(name, newChild))
    )
  }
}

fun addNamespaceFunctions(context: Context, entries: Map<String, String>): Context {
  val root = context.first()
  return listOf(
      entries.entries.fold(root) { a, b -> addNamespaceFunction(a, b.key.split("."), b.value)}
  )
      .plus(context.drop(1))
}

