package execution

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.parseTextBranching
import silentorb.imp.testing.errored

class ExecutionTest {
  @Test
  fun canExecute() {
    val code = """
      import imp.standard.math.*
      
      let output = + 10 6
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext())(code)) { result ->
      val graph = result.graph
      val value = executeToSingleValue(standardLibrary().implementation, graph)
      assertEquals(16, value)
    }
  }

  @Test
  fun supportsCompleteFunctionLibraries() {
    val code = """
      import imp.test.custom.*
      
      let main = newMonkey 1 -- The banana count logic is arbitrary
    """.trimIndent()
    handleRoot(errored, parseTextBranching(customLibraryContext())(code)) { result ->
      val graph = result.graph
      val value = executeToSingleValue(customLibrary().implementation, graph)
      assertEquals(2, value)
    }
  }

  @Test
  fun supportsCustomFunctions() {
    val code = """
      import imp.standard.*
      import imp.standard.math.*
      
      let add a:Int b:Int = + a b
      let output = add 1 2
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext())(code)) { result ->
      val value = executeToSingleValue(standardLibrary().implementation, result)
      assertEquals(3, value)
    }
  }
}
