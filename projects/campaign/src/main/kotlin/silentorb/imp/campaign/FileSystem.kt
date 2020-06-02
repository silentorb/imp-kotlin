package silentorb.imp.campaign

import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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

fun glob(pattern: String, uri: URI): List<Path> =
    glob(pattern, Paths.get(uri))

fun baseName(path: Path) =
    path.fileName.toString().split(".").first()

tailrec fun findContainingDirectory(indicatorFile: Path, path: Path): Path? =
    if (Files.isRegularFile(path.resolve(indicatorFile)))
      path
    else if (path.parent == null)
      null
    else
      findContainingDirectory(indicatorFile, path.parent)

fun findContainingWorkspaceDirectory(path: Path): Path? =
    findContainingDirectory(workspaceFilePath, path)

fun findContainingModule(path: Path): Path? =
    findContainingDirectory(moduleFilePath, path)
