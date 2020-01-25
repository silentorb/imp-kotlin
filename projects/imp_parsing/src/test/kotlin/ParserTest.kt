import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.emptyContext
import silentorb.imp.parsing.parser.parseText

class ParserTest {

  @Test
  fun canParseSimple() {
    val code = "output = 10"

    val context = emptyContext()

    handleRoot(errored, parseText(context)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(1, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun canParseTwoDefinitions() {
    val code = """
      intermediate = 10
      output = intermediate
    """.trimIndent()

    handleRoot(errored, parseText(emptyContext())(code)) { result ->
      val graph = result.graph
      assertEquals(3, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10, graph.values.values.first())
      assertTrue(graph.nodes.containsAll(setOf(1L, 2L, 3L)))
    }
  }

  @Test
  fun requiresANewlineBetweenDefinitions() {
    val code = "intermediate = 10 output = intermediate"
    expectError(TextId.expectedNewline, parseText(emptyContext())(code))
  }

  @Test
  fun preventsDuplicateSymbols() {
    val code = """
      output = 10
      output = output
    """.trimIndent()
    expectError(TextId.duplicateSymbol, parseText(emptyContext())(code))
  }

  @Test
  fun preventsMultipleGraphOutputs() {
    val code = """
      first = 10
      second = 10
    """.trimIndent()
    expectError(TextId.multipleGraphOutputs, parseText(emptyContext())(code))
  }

  @Test
  fun requiresAGraphOutput() {
    val code = """
    """.trimIndent()
    expectError(TextId.noGraphOutput, parseText(emptyContext())(code))
  }

  @Test
  fun supportsImportingSingleSymbols() {
    val code = """
      import silentorb.imp.test.simpleFunction
      
      output = simpleFunction
    """.trimIndent()
    handleRoot(errored, parseText(emptyContext())(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.functions.size)
    }
  }

  @Test
  fun supportsImportingNamespaces() {
    val code = """
      import silentorb.imp.test.*
      
      output = simpleFunction
    """.trimIndent()
  }

  @Test
  fun preventsImportsAfterDefinitions() {
    val code = """ 
      output = 10
      import silentorb.imp.test.simpleFunction
    """.trimIndent()
  }

  @Test
  fun supportsFunctionCallsWithArguments() {
//    val code = """
//      first = 10
//      second = 10
//    """.trimIndent()
//    expectError(TextId.noGraphOutput, parseText(emptyContext())(code))
  }
}
