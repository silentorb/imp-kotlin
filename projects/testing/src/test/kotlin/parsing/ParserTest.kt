import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import silentorb.imp.core.Input
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultParameter
import silentorb.imp.core.joinPaths
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.parser.*
import silentorb.imp.parsing.syntax.BurgType
import silentorb.imp.parsing.syntax.parseSyntax
import silentorb.imp.testing.errored
import silentorb.imp.testing.expectError

class ParserTest {

  @Test
  fun canParseSimple() {
    val code = "let output = 10"

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      assertEquals(3, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun canParseImports() {
    val code = "import silentorb.imp.test.simpleFunction"
    val (_, errors) = parseToDungeon("", simpleContext, code)
    errored(errors)
  }

  @Test
  fun canParseDecimalNumbers() {
    val code = "let output = 10.3"

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10.3f, graph.values.values.first())
    }
  }

  @Test
  fun supportsNegativeNumbers() {
    val code = "let output = -10.3"

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      assertEquals(-10.3f, graph.values.values.first())
    }
  }

  @Test
  fun canParseParenthesis() {
    val code = "let output = (10)"

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      assertEquals(3, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(2, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun canParseTwoDefinitions() {
    val code = """
      let intermediate = 10
      let output = intermediate
    """.trimIndent()

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      val intermediate = PathKey(localPath, "intermediate")
      val intermediateReference = PathKey(joinPaths(localPath, "output"), "intermediate1")
      assertEquals(1, graph.values.size)
      assertEquals(5, graph.connections.size)
      assertEquals(10, graph.values.values.first())
      assertTrue(graph.nodes.contains(intermediate))
      assertTrue(graph.nodes.contains(PathKey(localPath, "output")))
      assertTrue(graph.nodes.contains(PathKey(joinPaths(localPath, "intermediate"), "#literal1")))
    }
  }

  @Test
  fun canParseDefinitionsInAnyOrder() {
    val code = """
let output = intermediate
let intermediate = 10
    """.trimIndent()

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      val intermediate = PathKey(localPath, "intermediate")
      assertEquals(1, graph.values.size)
      assertEquals(5, graph.connections.size)
      assertEquals(10, graph.values.values.first())
      assertTrue(graph.nodes.contains(intermediate))
      assertTrue(graph.nodes.contains(PathKey(localPath, "output")))
      assertTrue(graph.nodes.contains(PathKey(joinPaths(localPath, "intermediate"), "#literal1")))
    }
  }

  @Test
  fun requiresANewlineBetweenDefinitions() {
    val code = "let intermediate = 10 let output = intermediate"
    expectError(TextId.expectedNewline, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsDuplicateSymbols() {
    val code = """
      let output = 10
      let output = output
    """.trimIndent()
    expectError(TextId.duplicateSymbol, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsEmptyExpressions() {
    val code = """
      let first =
    """.trimIndent()
    expectError(TextId.missingExpression, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsImportingMissingFunctions() {
    val code = """
      import silentorb.imp.test.simpleFunction
    """.trimIndent()
    expectError(TextId.importNotFound, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsEmptyImports() {
    val code = """
      import
    """.trimIndent()
    expectError(TextId.missingImportPath, parseToDungeon(emptyContext, code))
  }


  @Test
  fun preventsMisplacedWildcardsInImports() {
    val code = """
      import silentorb.imp.*.simpleFunction
    """.trimIndent()
    expectError(TextId.expectedImportOrLetKeywords, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsInvalidTokensInImports() {
    val code = """
      import silentorb.imp.10.simpleFunction
    """.trimIndent()
    expectError(TextId.unexpectedCharacter, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsImportsStartingWithADot() {
    val code = """
      import .silentorb.imp
    """.trimIndent()
    expectError(TextId.expectedIdentifier, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsImportsEndingWithADot() {
    val code = """
      import silentorb.imp.
    """.trimIndent()
    expectError(TextId.expectedIdentifierOrWildcard, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsImportsWithDoubleDots() {
    val code = """
      import silentorb..imp
    """.trimIndent()
    expectError(TextId.expectedIdentifierOrWildcard, parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsUnknownNullaryFunctions() {
    val code = """
      let output = simpleFunction
    """.trimIndent()
    assertUnknownFunctionError(parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsUnknownUnaryFunctions() {
    val code = """
      let output = + 10
    """.trimIndent()
    assertUnknownFunctionError(parseToDungeon(emptyContext, code))
  }

  @Test
  fun preventsMissingFunctions() {
    val code = """
      let output = simpleFunction 1 1
    """.trimIndent()
    assertUnknownFunctionError(parseToDungeon(emptyContext, code))
  }

  @Test
  fun supportsImportingSingleSymbols() {
    val code = """
      import silentorb.imp.test.simpleFunction
      
      let output = simpleFunction 1 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
    }
  }

  @Test
  fun supportsImportingNamespaces() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 1 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
    }
  }

  @Test
  fun preventsInvalidArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 1 1.0
    """.trimIndent()
    expectError(TextId.noMatchingSignature, parseToDungeon(simpleContext, code))
  }

  @Test
  fun supportsFunctionCallsWithArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction 32 5
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(5, graph.connections.size)
      val node2 = PathKey("output", "%application0")
      val node3 = PathKey("output", "#literal1")
      val node4 = PathKey("output", "#literal2")
      assertTrue(graph.connections[Input(destination = node2, parameter = "first")] == node3)
      assertTrue(graph.connections[Input(destination = node2, parameter = "second")] == node4)
    }
  }

  @Test
  fun supportsUsingTheSameNodeAsMultipleArguments() {
    val code = """
      import silentorb.imp.test.*
      let value = 32
      let output = simpleFunction value value
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(8, graph.nodes.size)
    }
  }

  @Test
  fun supportsDataStructures() {
    val code = """
      import silentorb.imp.test.*
      
      let output = something (Vector2i 3 10)
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(7, graph.nodes.size)
      assertEquals(8, graph.connections.size)
    }
  }

  @Test
  fun usesNodeTypeToDeduceOverloadSelection() {
    val code = """
      import silentorb.imp.test.*
      
      let value = 32
      let output = simpleFunction value 5
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(8, graph.nodes.size)
      assertEquals(8, graph.connections.size)
    }
  }

  @Test
  fun canParseComments() {
    val code = """
let value = 10

-- This is a comment
let output = value
"""

    handleRoot(errored, parseToDungeon(emptyContext, code)) { result ->
      val graph = result.graph
      assertEquals(6, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(5, graph.connections.size)
      assertEquals(10, graph.values.values.first())
    }
  }

  @Test
  fun supportsNamedArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction2 second = 1 first = 2.1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(5, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.entries.first { it.key.parameter == "second" }.value])
      assertEquals(2.1f, graph.values[graph.connections.entries.first { it.key.parameter == "first" }.value])
    }
  }

  @Test
  fun supportsMixedNamedArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction2 1 first = 2.1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
      assertEquals(5, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.entries.first { it.key.parameter == "second" }.value])
      assertEquals(2.1f, graph.values[graph.connections.entries.first { it.key.parameter == "first" }.value])
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

    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.connections.size)
      assertEquals(1, graph.values[graph.connections.entries.first { it.key.parameter == "second" }.value])
      assertEquals(2.1f, graph.values[graph.connections.entries.first { it.key.parameter == "first" }.value])
    }
  }

  @Test
  fun properlyHandlesInvalidArgumentTokens() {
    val code = "let output = 10 c"
    expectError(TextId.unknownFunction, parseToDungeon(emptyContext, code))
  }

  @Test
  fun handlesCascadingEffectsOfInvalidFunctionSignatures() {
    val code = """
import silentorb.imp.test.*
let a = simpleFunction2 1.1 2.1
let output = simpleFunction a (simpleFunction 3 3)
""".trimIndent()
    expectError(TextId.noMatchingSignature, parseToDungeon(simpleContext, code))
  }

  @Test
  fun tracksFunctionReturnTypes() {
    val code = """
import silentorb.imp.test.*
let a = simpleFunction2 2.1 1
let output = simpleFunction a (simpleFunction 3 3)
""".trimIndent()

    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
    }
  }

  @Test
  fun supportsMatchingArgumentOrderByType() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction2 1 2.0
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
    }
  }

  @Test
  fun supportsPiping() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction 1 1 . simpleFunction2 2.0 . simpleFunction2 3.0
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(11, graph.nodes.size)
    }
  }

  @Test
  fun supportsPipingReferences() {
    val nestedCode = """
      import silentorb.imp.test.*
      let first = 1
      let output = simpleFunction first 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, nestedCode)) { result ->
      val first = result.graph
      val pipingCode = """
      import silentorb.imp.test.*
      let first = 1
      let output = first . simpleFunction 1
    """.trimIndent()
      handleRoot(errored, parseToDungeon(simpleContext, pipingCode)) { result ->
        val second = result.graph
        assertEquals(2, (first.connections - second.connections.keys).size)
        assertEquals(3, (second.connections - first.connections.keys).size)
      }
    }
  }

  @Test
  fun supportsGroupedPiping() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction2 1.2 (simpleFunction2 3.0 2) . simpleFunction 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(11, graph.nodes.size)
      assertEquals(PathKey("output", "simpleFunction1"), graph.connections[Input(PathKey("output", "%application2"), defaultParameter)])
    }
  }

  @Test
  fun supportsPipingWithinGroups() {
    val code = """
      import silentorb.imp.test.*
      let output = simpleFunction2 1.2 (3.0 . simpleFunction2 2)
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(9, graph.nodes.size)
    }
  }

  @Test
  fun preventsDanglingPipeOperators() {
    val code = """
      import silentorb.imp.test.simpleFunction
      let bob = simpleFunction 1 1 .
      let output = bob
""".trimIndent()
    expectError(TextId.missingRighthandExpression, parseToDungeon(simpleContext, code))
  }

  @Test
  fun requiresPipeOperatorsToHaveAPrecedingExpression() {
    val code = """
      import silentorb.imp.test.simpleFunction
      let output = . simpleFunction 1 1
""".trimIndent()
    expectError(TextId.missingLefthandExpression, parseToDungeon(simpleContext, code))
  }

  @Test
  fun supportsNumericTypeConstraints() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure 10.0
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
    }
  }

  @Test
  fun preventsValuesAboveNumericConstraint() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure 10.6
    """.trimIndent()
    expectError(TextId.outsideTypeRange, parseToDungeon(simpleContext, code))
  }

  @Test
  fun preventsValuesBelowNumericConstraint() {
    val code = """
      import silentorb.imp.test.measure
      let output = measure -12.0
    """.trimIndent()
    expectError(TextId.outsideTypeRange, parseToDungeon(simpleContext, code))
  }

  @Test
  fun preventsInvalidFunctionsWithNoArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction eightPointFive 1.0
    """.trimIndent()
    expectError(TextId.noMatchingSignature, parseToDungeon(simpleContext, code))
  }

  @Test
  fun supportsFunctionsWithNoArguments() {
    val code = """
      import silentorb.imp.test.*
      
      let output = simpleFunction eight 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(5, graph.nodes.size)
    }
  }

  @Test
  fun supportsNestedDefinitions() {
    val code = """
      import silentorb.imp.test.*
      
      let first = {
        let nine = 9
        let result = simpleFunction eight nine
      }
      let output = first
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(12, graph.nodes.size)
    }
  }

  @Test
  fun preventsLeakingOfNestedDefinitionSymbols() {
    val code = """
      import silentorb.imp.test.*
      
      let first = {
        let nine = 9
        let result = simpleFunction eight nine
      }
      
      let output = simpleFunction first nine
    """.trimIndent()
    expectError(TextId.unknownFunction, parseToDungeon(simpleContext, code))
  }

  @Test
  fun returnsCorrectRangesWhenPiping() {
    val code1 = "let x = + 10 2"
    val (tokens1, tokenErrors1) = tokenizeAndSanitize("", code1)
    val (realm1, syntaxErrors1) = parseSyntax("", tokens1)
    val code2 = "let x = 1 .+ 2"
    val (tokens2, tokenErrors2) = tokenizeAndSanitize("", code2)
    val (realm2, syntaxErrors2) = parseSyntax("", tokens2)
    assert(tokenErrors1.plus(syntaxErrors1).plus(tokenErrors2).plus(syntaxErrors2).none())

    val block1 = realm1.burgs.values.first { it.type == BurgType.block }
    val block2 = realm2.burgs.values.first { it.type == BurgType.block }
    assertEquals(block1.range.end.index, block2.range.end.index)

    val application1 = realm1.burgs.values
        .filter { it.type == BurgType.application }.maxBy { it.range.length }!!
    val application2 = realm2.burgs.values
        .filter { it.type == BurgType.application }.maxBy { it.range.length }!!
    assertEquals(application1.range.start.index, application2.range.start.index)
    assertEquals(application1.range.end.index, application2.range.end.index)
  }

  @Test
  fun supportsOverloadsAcrossNamespaces() {
    val code = """
      import silentorb.imp.test.*
      import silentorb.imp.cat.*
      let output = overload 1
    """.trimIndent()
    handleRoot(errored, parseToDungeon(simpleContext, code)) { result ->
      val graph = result.graph
      assertEquals(4, graph.nodes.size)
    }
  }

  @Test
  fun preventsUnknownParameterTypes() {
    val code = """
      let method a: Madness = 1
    """.trimIndent()
    assertUnknownFunctionError(parseToDungeon(simpleContext, code))
  }
}
