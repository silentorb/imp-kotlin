import org.junit.Assert
import org.junit.Test
import silentorb.imp.parsing.ParsingError
import silentorb.imp.parsing.emptyContext
import silentorb.imp.parsing.handleRoot
import silentorb.imp.parsing.parseText

val errored = { errors: List<ParsingError> ->
  // This line should not be hit
  Assert.assertEquals(0, errors.size)
}

class ParserTest {

  @Test
  fun canParse() {
    val code = """
      output = 1.0f
    """.trimIndent()
    val context = emptyContext()

    handleRoot(errored, parseText(context)(code)) { result ->
      val graph = result.graph
      Assert.assertEquals(1, graph.nodes.size)
      Assert.assertEquals(1, graph.values.size)
      Assert.assertEquals(0, graph.connections.size)
    }
  }
}
