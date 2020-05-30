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

fun loadModuleInfo(path: Path): ModuleInfo {
  return ModuleInfo(
      config = loadYamlFile<ModuleConfig>(path.resolve(moduleFileName)) ?: ModuleConfig(),
      sourceFiles = glob("src/**/*.imp", path)
  )
}

fun loadModule(root: URI, library: Library, info: ModuleInfo): ParsingResponse<Module> {
  val config = info.config
  val sourceFiles = info.sourceFiles
  val (dungeons, errors) = loadSourceFiles(root, library, config, sourceFiles)
  return ParsingResponse(
      Module(
          dungeons = dungeons,
          fileNamespaces = config.fileNamespaces ?: false
      ),
      errors
  )
}

fun loadModuleInfos(root: URI, globPattern: String): Map<ModuleId, ModuleInfo> {
  val moduleDirectories = glob(globPattern, root)
      .filter { Files.isDirectory(it) }
      .filter { Files.exists(it.resolve(moduleFileName)) }

  return moduleDirectories
      .associate { path ->
        val info = loadModuleInfo(path)
        Pair(baseName(path), info)
      }
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
    val infos = workspaceConfig.modules
        .map { loadModuleInfos(root.toUri(), it) }
        .reduce { a, b -> a + b }

    val dependencies = infos
        .flatMap { (name, info) ->
          info.config.dependencies
              .map { provider ->
                Dependency(
                    dependent = name,
                    provider = provider
                )
              }
        }
        .toSet()

    val (stages, dependencyErrors) = arrangeModuleStages(infos.keys, dependencies)
    val modulePairs = stages
        .flatMap { stage ->
          stage.map { name ->
            Pair(name, loadModule(root.toUri(), library, infos[name]!!))
          }
        }
        .associate { it }

    val modules = modulePairs
        .mapValues { it.value.value }

    return CampaignResponse(
        value = Workspace(
            modules = modules,
            dependencies = dependencies
        ),
        campaignErrors = dependencyErrors,
        parsingErrors = modulePairs.flatMap { it.value.errors }
    )
  }
}
