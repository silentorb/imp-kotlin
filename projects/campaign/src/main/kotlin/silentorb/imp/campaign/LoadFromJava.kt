package silentorb.imp.campaign

import silentorb.imp.core.Namespace
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path

fun loadLibraryFromJava(rootPath: Path, javaImport: JavaImport): Namespace {
  val uri = File(File(rootPath.resolve(Path.of(javaImport.packagePath)).toString()).canonicalPath).toURI().toURL()
  val child = URLClassLoader(arrayOf(uri),
      Workspace::class.java.classLoader
  )
  val classToLoad = Class.forName(javaImport.classPath, false, child)
  val method = classToLoad.getDeclaredMethod(javaImport.method)
  val result = method.invoke(null)
  return result as? Namespace ?: throw Error()
}

// TODO: Eventually javaImports should be merged with module artifacts instead of being merged globally so javaImports respects dependencies
fun loadLibrariesFromJava(workspace: Workspace): List<Namespace> =
    workspace.modules.values.flatMap { module ->
      module.config.javaImports.map { javaImport ->
        loadLibraryFromJava(workspace.path, javaImport)
      }
    }
