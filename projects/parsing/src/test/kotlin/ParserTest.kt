import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import silentorb.imp.core.Connection
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultParameter
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.emptyContext
import silentorb.imp.parsing.parser.localPath
import silentorb.imp.parsing.parser.parseTextBranching
import silentorb.imp.testing.errored
import silentorb.imp.testing.expectError

class ParserTest {

  @Test
  fun canParseSimple() {
    val code = "let output = 10"

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
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

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(1, graph.connections.size)
      assertEquals(10.3f, graph.values.values.first())
    }
  }

  @Test
  fun supportsNegativeNumbers() {
    val code = "let output = -10.3"

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(-10.3f, graph.values.values.first())
    }
  }

  @Test
  fun canParseParenthesis() {
    val code = "let output = (10)"

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
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

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
      val graph = result.graph
      val intermediate = PathKey(localPath, "intermediate")
      val intermediateReference = PathKey(localPath + ".output", "intermediate1")
      assertEquals(4, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10, graph.values.values.first())
      assertTrue(graph.nodes.contains(intermediate))
      assertTrue(graph.nodes.contains(PathKey(localPath, "output")))
      assertTrue(graph.nodes.contains(PathKey(localPath + ".intermediate", "#literal1")))
      assertTrue(graph.references.containsKey(intermediateReference))
    }
  }

  @Test
  fun requiresANewlineBetweenDefinitions() {
    val code = "let intermediate = 10 let output = intermediate"
    expectError(TextId.expectedNewline, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsDuplicateSymbols() {
    val code = """
      let output = 10
      let output = output
    """.trimIndent()
    expectError(TextId.duplicateSymbol, parseTextBranching(emptyContext)(code))
  }

  @Ignore
  @Test
  fun preventsMultipleGraphOutputs() {
    val code = """
      let first = 10
      let second = 10
    """.trimIndent()
    expectError(TextId.multipleGraphOutputs, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsEmptyExpressions() {
    val code = """
      let first =
    """.trimIndent()
    expectError(TextId.missingExpression, parseTextBranching(emptyContext)(code))
  }


  @Test
  fun preventsImportingMissingFunctions() {
    val code = """
      import silentorb.imp.test.simpleFunction
    """.trimIndent()
    expectError(TextId.importNotFound, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsMisplacedWildcardsInImports() {
    val code = """
      import silentorb.imp.*.simpleFunction
    """.trimIndent()
    expectError(TextId.invalidToken, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsInvalidTokensInImports() {
    val code = """
      import silentorb.imp.10.simpleFunction
    """.trimIndent()
    expectError(TextId.unexpectedCharacter, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsImportsStartingWithADot() {
    val code = """
      import .silentorb.imp
    """.trimIndent()
    expectError(TextId.invalidToken, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsImportsEndingWithADot() {
    val code = """
      import silentorb.imp.
    """.trimIndent()
    expectError(TextId.invalidToken, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsImportsWithDoubleDots() {
    val code = """
      import silentorb..imp
    """.trimIndent()
    expectError(TextId.invalidToken, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsUnknownNullaryFunctions() {
    val code = """
      let output = simpleFunction
    """.trimIndent()
    expectError(TextId.unknownFunction, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsUnknownUnaryFunctions() {
    val code = """
      let output = + 10
    """.trimIndent()
    expectError(TextId.unknownFunction, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun preventsMissingFunctions() {
    val code = """
      let output = simpleFunction 1 1
    """.trimIndent()
    expectError(TextId.unknownFunction, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun supportsImportingSingleSymbols() {
    val code = """
      import silentorb.imp.test.simpleFunction
      
      let output = simpleFunction 1 1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
    }
  }

  @Test
  fun supportsImportingNamespaces() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 1 1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
    }
  }

  @Test
  fun preventsInvalidArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 1 1.0
    """.trimIndent()
    expectError(TextId.noMatchingSignature, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun supportsFunctionCallsWithArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 32 5
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(3, graph.connections.size)
      val node2 = PathKey("@local.output", "simpleFunction1")
      val node3 = PathKey("@local.output", "#literal1")
      val node4 = PathKey("@local.output", "#literal2")
      assertTrue(graph.connections.contains(Connection(destination = node2, source = node3, parameter = "first")))
      assertTrue(graph.connections.contains(Connection(destination = node2, source = node4, parameter = "second")))
    }
  }

  @Test
  fun supportsUsingTheSameNodeAsMultipleArguments() {
    val code = """
      import silentorb.imp.test.*
      let value = 32
      let output = simpleFunction value value
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(6, graph.nodes.size)
    }
  }

  @Test
  fun supportsDataStructures() {
    val code = """
      import silentorb.imp.test.*
      
      let output = something (Vector2i 3 10)
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(4, graph.connections.size)
    }
  }

  @Test
  fun usesNodeTypeToDeduceOverloadSelection() {
    val code = """
      import silentorb.imp.test.*
      
      let value = 32
      let output = simpleFunction value 5
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(6, graph.nodes.size)
      assertEquals(4, graph.connections.size)
    }
  }

  @Test
  fun canParseComments() {
    val code = """
let value = 10

-- This is a comment
let output = value
"""

    handleRoot(errored, parseTextBranching(emptyContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun supportsNamedArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction2 second = 1 first = 2.1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(3, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.first { it.parameter == "second" }.source])
      assertEquals(2.1f, graph.values[graph.connections.first { it.parameter == "first" }.source])
    }
  }

  @Test
  fun supportsMixedNamedArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction2 1 first = 2.1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(3, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.first { it.parameter == "second" }.source])
      assertEquals(2.1f, graph.values[graph.connections.first { it.parameter == "first" }.source])
    }
  }

  @Test
  fun supportsMultiLineExpressions() {
    val code = """
import silentorb.imp.test.*

let output = simpleFunction2 
  second = 1
  first = (2.1)
""".trimIndent()

    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
      assertEquals(3, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.first { it.parameter == "second" }.source])
      assertEquals(2.1f, graph.values[graph.connections.first { it.parameter == "first" }.source])
    }
  }

  @Test
  fun properlyHandlesInvalidArgumentTokens() {
    val code = "let output = 10 c"
    expectError(TextId.unknownFunction, parseTextBranching(emptyContext)(code))
  }

  @Test
  fun handlesCascadingEffectsOfInvalidFunctionSignatures() {
    val code = """
import silentorb.imp.test.*
let a = simpleFunction2 1.1 2.1
let output = simpleFunction a (simpleFunction 3 3)
""".trimIndent()
    expectError(TextId.noMatchingSignature, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun tracksFunctionReturnTypes() {
    val code = """
import silentorb.imp.test.*
let a = simpleFunction2 2.1 1
let output = simpleFunction a (simpleFunction 3 3)
""".trimIndent()

    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
    }
  }

  @Test
  fun supportsMatchingArgumentOrderByType() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction2 1 2.0
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
    }
  }

  @Test
  fun supportsPiping() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction 1 1 . simpleFunction2 2.0 . simpleFunction2 3.0
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(8, graph.nodes.size)
    }
  }

  @Test
  fun supportsPipingReferences() {
    val code = """
      import silentorb.imp.test.*
      let first = 1
      let output = first . simpleFunction 1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
    }
  }

  @Test
  fun preventsDanglingPipeOperators() {
    val code = """
      import silentorb.imp.test.simpleFunction
      let bob = simpleFunction 1 1 .
      let output = bob
""".trimIndent()
    expectError(TextId.missingExpression, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun requiresPipeOperatorsToHaveAPrecedingExpression() {
    val code = """
      import silentorb.imp.test.simpleFunction
      let output = . simpleFunction 1 1
""".trimIndent()
    expectError(TextId.missingExpression, parseTextBranching(simpleContext)(code))
  }

//  @Test
//  fun supportsSelectingTheLastOutput() {
//    val code = """
//      import silentorb.imp.test.*
//      let first = 1
//      let output = first . simpleFunction 1
//    """.trimIndent()
//    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
//      val graph = result.graph
//    }
//  }

  @Test
  fun supportsNumericTypeConstraints() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure 10.0
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
    }
  }

  @Test
  fun preventsValuesAboveNumericConstraint() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure 10.6
    """.trimIndent()
    expectError(TextId.outsideTypeRange, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun preventsValuesBelowNumericConstraint() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure -12.0
    """.trimIndent()
    expectError(TextId.outsideTypeRange, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun preventsInvalidFunctionsWithNoArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction eightPointFive 1.0
    """.trimIndent()
    expectError(TextId.noMatchingSignature, parseTextBranching(simpleContext)(code))
  }

  @Test
  fun supportsFunctionsWithNoArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction eight 1
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
    }
  }

  @Ignore()
  @Test
  fun supportsCustomFunctions() {
    val code = """
      import silentorb.imp.test.*
      let add a:Int b:Int = a + b
      let output = add 1 2
    """.trimIndent()
    handleRoot(errored, parseTextBranching(simpleContext)(code)) { result ->
      val graph = result.graph
      assertEquals(7, graph.nodes.size)
    }
  }
}
