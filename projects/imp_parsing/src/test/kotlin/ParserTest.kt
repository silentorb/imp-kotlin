import org.junit.Assert.*
import org.junit.Test
import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.parsing.general.ParsingError
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.expectErrors
import silentorb.imp.parsing.parser.emptyContext
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.parseText

val errored = { errors: List<ParsingError> ->
  // This line should not be hit
  assertEquals(0, errors.size)
}

val shouldHaveErrored = { ->
  // This line should not be hit
  assertTrue(false)
}

val code = """
      output = 10
    """.trimIndent()

class ParserTest {

  @Test
  fun detectsBadCharacters() {
    expectErrors(shouldHaveErrored, tokenize("$")) { errors ->
      assertEquals(1, errors.size)
      assertEquals(TextId.unexpectedCharacter, errors.first().message)
    }
  }

  @Test
  fun canParseAnEmptyString() {
    handleRoot(errored, tokenize("")) { tokens ->
      assertEquals(0, tokens.size)
    }
  }

  @Test
  fun canParseEmptyWhitespace() {
    handleRoot(errored, tokenize("   ")) { tokens ->
      assertEquals(0, tokens.size)
    }
  }

  @Test
  fun canTokenizeWithInt() {
    handleRoot(errored, tokenize(code)) { tokens ->
      assertEquals(3, tokens.size)
      assertEquals("output", tokens.first().text)
    }
  }

  @Test
  fun canTokenizeWithFloat() {
    val code = """
      output = 10.3
    """.trimIndent()
    handleRoot(errored, tokenize(code)) { tokens ->
      assertEquals(3, tokens.size)
      assertEquals("output", tokens.first().text)
    }
  }

  @Test
  fun canParse() {
    val context = emptyContext()

    handleRoot(errored, parseText(context)(code)) { result ->
      val graph = result.graph
      assertEquals(2, graph.nodes.size)
      assertEquals(1, graph.values.size)
      assertEquals(1, graph.connections.size)
    }
  }
}
