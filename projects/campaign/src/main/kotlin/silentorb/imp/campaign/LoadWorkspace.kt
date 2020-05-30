package silentorb.imp.campaign

import silentorb.imp.core.Dungeon
import silentorb.imp.execution.Library
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.parser.parseText
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

const val workspaceFileName = "workspace.yaml"
const val moduleFileName = "module.yaml"

fun loadSourceFiles(root: URI, library: Library, moduleConfig: ModuleConfig, sourceFiles: List<Path>): ParsingResponse<Map<DungeonId, Dungeon>> {
  val context = listOf(library.namespace)
  val results = sourceFiles.map { path ->
    val code = Files.readString(path, StandardCharsets.UTF_8)!!
    val (dungeon, errors) = parseText(root.relativize(path.toUri()), context)(code)
    Pair(Pair(baseName(path), dungeon), errors)
  }
  return ParsingResponse(
      results.associate { it.first },
      results.flatMap { it.second }
  )
}

fun loadModule(root: URI, library: Library, path: Path): ParsingResponse<Module> {
  val sourceFiles = glob("src/**/*.imp", path)
  val moduleConfig = loadYamlFile<ModuleConfig>(path.resolve(moduleFileName)) ?: ModuleConfig()
  val (dungeons, errors) = loadSourceFiles(root, library, moduleConfig, sourceFiles)
  return ParsingResponse(
      Module(
          dungeons = dungeons,
          fileNamespaces = moduleConfig.fileNamespaces ?: false
      ),
      errors
  )
}

fun loadModules(library: Library, root: Path, globPattern: String): ParsingResponse<Map<ModuleId, Module>> {
  val moduleDirectories = glob(globPattern, root)
      .filter { Files.isDirectory(it) }
      .filter { Files.exists(it.resolve(moduleFileName)) }

  val modulePairs = moduleDirectories
      .map { path ->
        val (module, errors) = loadModule(root.toUri(), library, path)
        Pair(Pair(baseName(path), module), errors)
      }
  return ParsingResponse(
      modulePairs.associate { it.first },
      modulePairs.flatMap { it.second }
  )
}

fun loadWorkspace(library: Library, root: Path): CampaignResponse<Workspace> {
  val workspaceFilePath = root.resolve(workspaceFileName)
  val workspaceConfig = loadYamlFile<WorkspaceConfig>(workspaceFilePath)
  return if (workspaceConfig == null)
    CampaignResponse(
        value = emptyWorkspace,
        campaignErrors = listOf(CampaignError(CampaignText.fileNotFound, arguments = listOf(workspaceFilePath))),
        parsingErrors = listOf()
    )
  else {
    val modulePairs = workspaceConfig.modules
        .map { loadModules(library, root, it) }

    val modules = modulePairs
        .map { it.value }
        .reduce { a, b -> a + b }

    return CampaignResponse(
        value = Workspace(
            modules = modules,
            dependencies = setOf()
        ),
        campaignErrors = listOf(),
        parsingErrors = modulePairs.flatMap { it.errors }
    )
  }
}
