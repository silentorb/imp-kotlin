import org.junit.Assert.assertEquals
import org.junit.Test
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.parseTextBranching
import silentorb.imp.testing.errored

class ExecutionTest {
  @Test
  fun canExecute() {
    val code = """
      import imp.math.*
      
      let output = + 10 6
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext())(code)) { result ->
      val graph = result.graph
      val value = executeToSingleValue(standardLibrary(), graph)
      assertEquals(16, value)
    }
  }

  @Test
  fun supportsCompleteFunctionLibraries() {
    val code = """
      import imp.test.custom.*
      
      let main = newMonkey 1 -- The banana count logic is arbitrary
    """.trimIndent()
    handleRoot(errored, parseTextBranching(customLibraryContext)(code)) { result ->
      val graph = result.graph
      val value = executeToSingleValue(customLibrary.implementation, graph)
      assertEquals(2, value)
    }
  }
}
