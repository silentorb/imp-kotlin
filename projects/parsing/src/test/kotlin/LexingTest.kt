import org.junit.Assert
import org.junit.Test
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.tokenize
import silentorb.imp.testing.errored
import silentorb.imp.testing.expectError

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
  fun returnsProperRanges() {
    handleRoot(errored, tokenize("let")) { tokens ->
      val range = tokens.first().range
      Assert.assertEquals(0, range.start.index)
      Assert.assertEquals(3, range.end.index)
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
  fun canTokenizeLiteralZero() {
    val code = "0"

    handleRoot(errored, tokenize(code)) { tokens ->
      Assert.assertEquals(1, tokens.size)
      Assert.assertEquals(Rune.literalInteger, tokens.first().rune)
    }
  }

  @Test
  fun preventsTokenizingZeroLeadingInteger() {
    val code = "01"

    expectError(TextId.unexpectedCharacter, tokenize(code))
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
