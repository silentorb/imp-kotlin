import org.junit.Test
import java.util.*

val random = Random(100)

fun generateNamespace(): Map<String, Int> =
    (0..1000).associate { Pair(random.nextInt().toString(), random.nextInt()) }

fun newNamespaceStack() =
    (0..4).map { generateNamespace() }

tailrec fun resolveSymbolInStack(stack: List<Map<String, Int>>, index: Int, key: String): Int? =
    if (index < 0)
      null
    else
      stack[index][key] ?: resolveSymbolInStack(stack, index - 1, key)

fun getRandomKey(keys: List<String>) =
    if (random.nextBoolean())
      keys[random.nextInt(keys.size - 1)]
    else
      random.nextInt().toString()

fun printValue(value: Int?) {
  if (value != null && value > 1000 && value < 100000)
    print(value.toString().first()) // To prevent optimizing away
}

const val iterations = 10000
fun namespaceStack() {
  val start = System.currentTimeMillis()
  for (i in 0 until iterations) {
    val stack = newNamespaceStack()
    val keys = stack.flatMap { it.keys }
    for (j in 0 until 100) {
      val key = getRandomKey(keys)
      val value = resolveSymbolInStack(stack, stack.size - 1, key)
      printValue(value)
    }
  }
  val end = System.currentTimeMillis()
  val duration = end - start
  println("")
  println("Recursion duration:\t\t$duration")
}

fun mergedNamespaces() {
  val start = System.currentTimeMillis()
  for (i in 0 until iterations) {
    val stack = newNamespaceStack()
    val keys = stack.flatMap { it.keys }
    val namespace = stack.reduce { a, b -> a + b }
    for (j in 0 until 100) {
      val key = getRandomKey(keys)
      val value = namespace[key]
      printValue(value)
    }
  }
  val end = System.currentTimeMillis()
  val duration = end - start
  println("")
  println("Merging duration:\t\t$duration")
}

class BenchmarkTest {
  @Test
  fun stackResolutionComparison() {
    namespaceStack()
    mergedNamespaces()
  }
}
