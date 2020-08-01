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
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val graph = result.namespace
      val value = executeToSingleValue(context, graph)
      assertEquals(16, value)
    }
  }

  @Test
  fun supportsCompleteFunctionLibraries() {
    val code = """
      import imp.test.custom.*
      
      let main = newMonkey 1 -- The banana count logic is arbitrary
    """.trimIndent()
    val context = customTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { dungeon ->
      val value = executeToSingleValue(context, dungeon)
      assertEquals(2, value)
    }
  }

  // This test isn't testing much because the bug it was originally built for
  // ended up involving function values, which would need a more complicated test
  @Test
  fun supportsTypeAliases() {
    val code = """
      import imp.test.custom.*
      
      let main = modMeasure 1.0
    """.trimIndent()
    val context = customTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { dungeon ->
      val value = executeToSingleValue(context, dungeon)
      assertEquals(3, value)
    }
  }


  @Test
  fun canExecuteNullaryFunctions() {
    val code = """
      import imp.test.custom.*
      
      let main = newMonkey newBananaCount
    """.trimIndent()
    val context = customTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val graph = result.namespace
      val value = executeToSingleValue(context, graph)
      assertEquals(3, value)
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
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val value = executeToSingleValue(context, result)
      assertEquals(3, value)
    }
  }

  @Test
  fun supportsNestedCustomFunctions() {
    val code = """
      import imp.standard.*
      import imp.standard.math.*
      
      let output = { 
        let add a:Float = + a 2.0
        let main = add 1.0
      }
    """.trimIndent()
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val value = executeToSingleValue(context, result)
      assertEquals(3.0f, value)
    }
  }

  @Test
  fun supportsAppliedVariadicFunctions() {
    val code = """
      import imp.standard.math.*
      
      let output = + 1 2 3
    """.trimIndent()
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(simpleContext(), code)) { result ->
      val graph = result.namespace
      val value = executeToSingleValue(context, graph)
      assertEquals(6, value)
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
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val value = executeToSingleValue(context, result)
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
    val context = standardTestContext()
    handleRoot(errored, parseToDungeon(context, code)) { result ->
      val value = executeToSingleValue(context, result)
      assertEquals(1.2f, value)
    }
  }

}
