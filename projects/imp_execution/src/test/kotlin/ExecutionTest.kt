import org.junit.Assert.assertEquals
import org.junit.Test
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.parseText
import silentorb.imp.testing.errored

class ExecutionTest {
  @Test
  fun canExecute() {
    val code = """
      import imp.math.*
      
      let output = + 10 6
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      val value = executeToSingleValue(standardLibrary, graph)
      assertEquals(16, value)
    }
  }
}
