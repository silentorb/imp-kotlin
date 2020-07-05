package execution

import handleRoot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.parsing.parser.parseToDungeon
import silentorb.imp.testing.errored

class ExecutionTest {
  @Test
  fun canExecute() {
    val code = """
      import imp.standard.math.*
      
      let output = + 10 6
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext(), code)) { result ->
      val graph = result.graph
      val library = standardLibrary()
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, graph)
      assertEquals(16, value)
    }
  }

  @Test
  fun supportsCompleteFunctionLibraries() {
    val code = """
      import imp.test.custom.*
      
      let main = newMonkey 1 -- The banana count logic is arbitrary
    """.trimIndent()
    val library = customLibrary()
    handleRoot(errored, parseToDungeon(listOf(library.namespace), code)) { dungeon ->
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, dungeon)
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
    val library = standardLibrary()
    handleRoot(errored, parseToDungeon(listOf(library.namespace), code)) { result ->
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, result)
      assertEquals(3, value)
    }
  }

  @Disabled
  @Test
  fun supportsDefiningUnions() {
    val code = """
      import imp.standard.*
   
      type Number = union Int Float
      let foo a: Number = a
      let output = foo 1.2
    """.trimIndent()
    val library = standardLibrary()
    handleRoot(errored, parseToDungeon(listOf(library.namespace), code)) { result ->
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, result)
      assertEquals(1.2f, value)
    }
  }

  @Disabled
  @Test
  fun supportsDefiningStructures() {
    val code = """
      import imp.standard.*
   
      data Foo
        a: Int
        b: Float

      let output = Foo 3 1.2
    """.trimIndent()
    val library = standardLibrary()
    handleRoot(errored, parseToDungeon(listOf(library.namespace), code)) { result ->
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, result)
      assertEquals(1.2f, value)
    }
  }

  @Disabled
  @Test
  fun supportsVarArgs() {
    val code = """
      import imp.standard.*
      
      let output = listOf(4, 5, 7)
    """.trimIndent()
    val library = standardLibrary()
    handleRoot(errored, parseToDungeon(listOf(library.namespace), code)) { result ->
      val value = executeToSingleValue(listOf(library.namespace), library.implementation, result)
      assertEquals(1.2f, value)
    }
  }
}
