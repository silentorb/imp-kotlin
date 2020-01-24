import org.junit.Assert
import org.junit.Test
import silentorb.imp.parsing.Context
import silentorb.imp.parsing.parseText

class ParserTest {

  @Test fun canParse() {
    val code = """
      output = 1.0f
    """.trimIndent()
    val context = Context(
        functions = mapOf()
    )
    val graph = parseText(context)(code)
    Assert.assertEquals(1, graph.nodes.size)
    Assert.assertEquals(1, graph.values.size)
    Assert.assertEquals(0, graph.connections.size)
  }
}
