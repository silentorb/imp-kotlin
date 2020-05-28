import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.lexer.stripWhitespace
import silentorb.imp.parsing.lexer.tokenize

class LexingTest {

  @Test
  fun canParseAnEmptyString() {
    val tokens = tokenize("")
    assertEquals(0, tokens.size)
  }

  @Test
  fun canParseEmptyWhitespace() {
    val tokens = tokenize("   ")
    assertEquals(1, tokens.size)
    assertEquals(Rune.whitespace, tokens[0].rune)
  }

  @Test
  fun returnsProperRanges() {
    val tokens = tokenize("let")
    val range = tokens.first().range
    assertEquals(0, range.start.index)
    assertEquals(3, range.end.index)
  }

  @Test
  fun canTokenizeWithInt() {
    val code = "output = 10"

    val tokens = stripWhitespace(tokenize(code))
    assertEquals(3, tokens.size)
    assertEquals("output", tokens.first().value)
    assertEquals(tokens[1].range.start.index + 1, tokens[1].range.end.index)
  }

  @Test
  fun canTokenizeLiteralZero() {
    val code = "0"

    val tokens = tokenize(code)
    assertEquals(1, tokens.size)
    assertEquals(Rune.literalInteger, tokens.first().rune)
  }

  @Test
  fun canTokenizeComments() {
    val code = "-- This is a comment"

    val tokens = tokenize(code)
    assertEquals(1, tokens.size)
    assertEquals(Rune.comment, tokens.first().rune)
    assertEquals("-- This is a comment", tokens.first().value)
  }

  @Test
  fun preventsTokenizingZeroLeadingInteger() {
    val code = "01"

    val tokens = tokenize(code)
    assertTrue(tokens.any { it.rune == Rune.bad })
  }

  @Test
  fun canTokenizeWithFloat() {
    val code = "output = 10.3"
    val tokens = tokenize(code)
    assertEquals(5, tokens.size)
    assertEquals("output", tokens.first().value)
  }

  @Test
  fun canTokenizeWithParenthesis() {
    val code = "output = (10)"
    val tokens = tokenize(code)
    assertEquals(7, tokens.size)
  }

  @Test
  fun supportsImportingSyntax() {
    val code = """
      import silentorb.imp.test.simpleFunction
      import silentorb.imp.test.*
    """.trimIndent()
    val tokens = tokenize(code)
      assertEquals(1, tokens.count { it.rune == Rune.operator })
  }

  @Test
  fun preventsNumbersBleedingIntoIdentifiers() {
    val code = "1c"

    val tokens = tokenize(code)
    assertEquals(1, tokens.size)
    assertTrue(tokens.any { it.rune == Rune.bad })
  }
}
