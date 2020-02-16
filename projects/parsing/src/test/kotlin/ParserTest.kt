import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import silentorb.imp.core.Connection
import silentorb.imp.core.floatKey
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.emptyContext
import silentorb.imp.parsing.parser.parseText
import silentorb.imp.testing.errored
import silentorb.imp.testing.expectError

class ParserTest {

  @Test
  fun canParseSimple() {
    val code = "let output = 10"

    handleRoot(errored, parseText(emptyContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(1, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun canParseDecimalNumbers() {
    val code = "let output = 10.3"

    handleRoot(errored, parseText(emptyContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(1, graph.types.size)
      assertEquals(1, graph.connections.size)
      assertEquals(10.3f, graph.values.values.first())
      assertEquals(floatKey, graph.types.values.first())
    }
  }

  @Test
  fun canParseParenthesis() {
    val code = "let output = (10)"

    handleRoot(errored, parseText(emptyContext)(code)) { result ->
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
      let intermediate = 10
      let output = intermediate
    """.trimIndent()

    handleRoot(errored, parseText(emptyContext)(code)) { result ->
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
    val code = "let intermediate = 10 let output = intermediate"
    expectError(TextId.expectedNewline, parseText(emptyContext)(code))
  }

  @Test
  fun preventsDuplicateSymbols() {
    val code = """
      let output = 10
      let output = output
    """.trimIndent()
    expectError(TextId.duplicateSymbol, parseText(emptyContext)(code))
  }

  @Test
  fun preventsMultipleGraphOutputs() {
    val code = """
      let first = 10
      let second = 10
    """.trimIndent()
    expectError(TextId.multipleGraphOutputs, parseText(emptyContext)(code))
  }

  @Test
  fun preventsImportingMissingFunctions() {
    val code = """
      import silentorb.imp.test.simpleFunction
    """.trimIndent()
    expectError(TextId.importNotFound, parseText(emptyContext)(code))
  }

  @Test
  fun preventsMisplacedWildcardsInImports() {
    val code = """
      import silentorb.imp.*.simpleFunction
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun preventsInvalidTokensInImports() {
    val code = """
      import silentorb.imp.10.simpleFunction
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun preventsImportsStartingWithADot() {
    val code = """
      import .silentorb.imp
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun preventsImportsEndingWithADot() {
    val code = """
      import silentorb.imp.
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun preventsImportsWithDoubleDots() {
    val code = """
      import silentorb..imp
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun preventsUnknownFunctions() {
    val code = """
      let output = simpleFunction
    """.trimIndent()
    expectError(TextId.unknownFunction, parseText(emptyContext)(code))
  }

  @Test
  fun supportsImportingSingleSymbols() {
    val code = """
      import silentorb.imp.test.simpleFunction
      
      let output = simpleFunction
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.types.size)
    }
  }

  @Test
  fun supportsImportingNamespaces() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.types.size)
    }
  }

  @Ignore
  @Test
  fun preventsImportsAfterDefinitions() {
    val code = """ 
      let output = 10
      import silentorb.imp.test.simpleFunction
    """.trimIndent()
    expectError(TextId.invalidToken, parseText(emptyContext)(code))
  }

  @Test
  fun supportsFunctionCallsWithArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 32 5
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(3, graph.connections.size)
      assertEquals(3, graph.types.size)
      assertTrue(graph.connections.contains(Connection(destination = 2, source = 3, parameter = "first")))
      assertTrue(graph.connections.contains(Connection(destination = 2, source = 4, parameter = "second")))
    }
  }

  @Test
  fun supportsDataStructures() {
    val code = """
      import silentorb.imp.test.*
      
      let output = something (Vector2i 3 10)
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(4, graph.connections.size)
      assertEquals(4, graph.types.size)
    }
  }

  @Test
  fun usesNodeTypeToDeduceOverloadSelection() {
    val code = """
      import silentorb.imp.test.*
      
      let value = 32
      let output = simpleFunction value 5
    """.trimIndent()
    handleRoot(errored, parseText(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(4, graph.connections.size)
      assertEquals(5, graph.types.size)
    }
  }
}
