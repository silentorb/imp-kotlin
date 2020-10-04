package campaign

import silentorb.imp.core.Namespace
import java.io.File
import java.net.URL
import java.net.URLClassLoader

fun loadTempLibrary(): Namespace {
  val uri = File("out/production/classes").toURI().toURL()
  val child = URLClassLoader(arrayOf(uri),
      CampaignTest::class.java.classLoader
  )
  val classToLoad = Class.forName("silentorb.imp.testing.library.LibraryKt", false, child)
  val method = classToLoad.getDeclaredMethod("newTestLibrary")
//  val instance = classToLoad.newInstance()
  val result = method.invoke(null)
  throw Error()
}
