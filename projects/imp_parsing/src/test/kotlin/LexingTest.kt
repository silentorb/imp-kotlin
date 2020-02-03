import org.junit.Assert
import org.junit.Test
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.testing.errored

class LexingTest {

  @Test
  fun canParseAnEmptyString() {
    handleRoot(errored, tokenize("")) { tokens ->
      Assert.assertEquals(0, tokens.size)
    }
  }

  @Test
  fun canParseEmptyWhitespace() {
    handleRoot(errored, tokenize("   ")) { tokens ->
      Assert.assertEquals(0, tokens.size)
    }
  }

  @Test
  fun canTokenizeWithInt() {
    val code = "output = 10"

    handleRoot(errored, tokenize(code)) { tokens ->
      Assert.assertEquals(3, tokens.size)
      Assert.assertEquals("output", tokens.first().value)
    }
  }

  @Test
  fun canTokenizeWithFloat() {
    val code = "output = 10.3"

    handleRoot(errored, tokenize(code)) { tokens ->
      Assert.assertEquals(3, tokens.size)
      Assert.assertEquals("output", tokens.first().value)
    }
  }

  @Test
  fun canTokenizeWithParenthesis() {
    val code = "output = (10)"

    handleRoot(errored, tokenize(code)) { tokens ->
      Assert.assertEquals(5, tokens.size)
    }
  }

  @Test
  fun supportsImportingSyntax() {
    val code = """
      import silentorb.imp.test.simpleFunction
      import silentorb.imp.test.*
    """.trimIndent()
    handleRoot(errored, tokenize(code)) { tokens ->
      Assert.assertEquals(1, tokens.count { it.rune == Rune.operator })
    }
  }
}
