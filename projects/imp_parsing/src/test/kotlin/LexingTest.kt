import org.junit.Assert
import org.junit.Test
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.lexer.tokenize

class LexingTest {

  @Test
  fun detectsBadCharacters() {
    expectError(TextId.unexpectedCharacter, tokenize("$"))
  }

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
}
