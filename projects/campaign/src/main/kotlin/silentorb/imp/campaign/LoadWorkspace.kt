package silentorb.imp.campaign

import silentorb.imp.core.Dungeon
import silentorb.imp.execution.Library
import silentorb.imp.parsing.general.PartitionedResponse
import silentorb.imp.parsing.parser.parseDungeon
import silentorb.imp.parsing.parser.parseText
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

const val workspaceFileName = "workspace.yaml"
const val moduleFileName = "module.yaml"

fun loadSourceFiles(library: Library, moduleConfig: ModuleConfig, sourceFiles: List<Path>): PartitionedResponse<Map<DungeonId, Dungeon>> {
  val context = listOf(library.namespace)
  val results = sourceFiles.map { path ->
    val code = Files.readString(path, StandardCharsets.UTF_8)!!
    val (dungeon, errors) = parseText(context)(code)
    Pair(Pair(path.fileName.toString(), dungeon), errors)
  }
  return PartitionedResponse(
      results.associate { it.first },
      results.flatMap { it.second }
  )
}

fun loadModule(library: Library, path: Path): Module {
  val sourceFiles = glob("src/**/*.imp", path)
  val moduleConfig = loadYamlFile<ModuleConfig>(path.resolve(moduleFileName)) ?: ModuleConfig()
  val (dungeons, errors) = loadSourceFiles(library, moduleConfig, sourceFiles)
  return Module(
      dungeons = dungeons,
      fileNamespaces = moduleConfig.fileNamespaces ?: false
  )
}

fun loadModules(library: Library, origin: Path, globPattern: String): Map<ModuleId, Module> {
  val moduleDirectories = glob(globPattern, origin)
      .filter { Files.isDirectory(it) }
      .filter { Files.exists(it.resolve(moduleFileName)) }

  return moduleDirectories
      .associate { path -> Pair(path.fileName.toString(), loadModule(library, path)) }
}

fun loadWorkspace(library: Library, workspaceDirectoryPath: Path): CampaignResponse<Workspace> {
  val workspaceFilePath = workspaceDirectoryPath.resolve(workspaceFileName)
  val workspaceConfig = loadYamlFile<WorkspaceConfig>(workspaceFilePath)
  return if (workspaceConfig == null)
    CampaignResponse(emptyWorkspace, listOf(CampaignError(CampaignText.fileNotFound, arguments = listOf(workspaceFilePath))))
  else {
    val modules = workspaceConfig.modules
        .map { loadModules(library, workspaceDirectoryPath, it) }
        .reduce { a, b -> a + b }
    return CampaignResponse(
        value = Workspace(
            modules = modules,
            dependencies = setOf()
        ),
        errors = listOf()
    )
  }
}
