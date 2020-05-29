package silentorb.imp.campaign

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private var globalYamlMapper: YAMLMapper? = null
private var afterburnerModule: AfterburnerModule? = null

fun getAfterburnerModule(): AfterburnerModule {
  if (afterburnerModule == null) {
    afterburnerModule = AfterburnerModule()
    afterburnerModule!!.setUseValueClassLoader(false)
  }
  return afterburnerModule!!
}

fun getYamlObjectMapper(): YAMLMapper {
  if (globalYamlMapper == null) {
    val mapper = YAMLMapper()
    mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
    mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
    mapper.registerModule(KotlinModule())
    mapper.registerModule(getAfterburnerModule())
    globalYamlMapper = mapper
    return mapper
  }

  return globalYamlMapper!!
}

inline fun <reified T> loadYamlFile(path: Path): T? {
  if (Files.isRegularFile(path)) {
    val mapper = getYamlObjectMapper()
    return Files.newBufferedReader(path).use { buffer ->
      val text = buffer.readText()
      if (text.matches(Regex("^\\s*$")))
        null
      else
        mapper.readValue(text, T::class.java)
    }
  }

  return null
}

fun getResourceStream(name: String): InputStream {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResourceAsStream(name)
}

inline fun <reified T> loadYamlResource(path: String): T {
  val content = getResourceStream(path)
  return getYamlObjectMapper().readValue(content, T::class.java)
}

inline fun <reified T> loadYamlResource(path: String, typeref: TypeReference<T>): T {
  val content = getResourceStream(path)
  return getYamlObjectMapper().readValue(content, typeref)
}
