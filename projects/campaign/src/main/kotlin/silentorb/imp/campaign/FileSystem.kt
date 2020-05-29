package silentorb.imp.campaign

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path

typealias PathFilter = (Path) -> Boolean

fun newGlobMatcher(pattern: String, origin: Path): PathFilter {
  val matcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
  return { child ->
    val relative = origin.relativize(child)
    matcher.matches(relative)
  }
}

fun glob(filter: PathFilter, path: Path): List<Path> {
  val selfMatch = if (filter(path))
    listOf(path)
  else
    listOf()

  val children = File(path.toUri()).listFiles()
      ?.flatMap { glob(filter, it.toPath()) }
      ?: listOf()

  return selfMatch + children
}

fun glob(pattern: String, path: Path): List<Path> =
    glob(newGlobMatcher(pattern, path), path)
