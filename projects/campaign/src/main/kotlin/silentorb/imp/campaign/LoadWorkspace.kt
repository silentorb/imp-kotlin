package silentorb.imp.campaign

import silentorb.imp.core.*
import silentorb.imp.execution.Library
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.parser.parseDungeon
import silentorb.imp.parsing.parser.toTokenGraph
import silentorb.imp.parsing.parser.tokenizeAndSanitize
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val workspaceFileName = "workspace.yaml"
const val moduleFileName = "module.yaml"

fun loadSourceFiles(moduleName: String, root: Path, context: Context, moduleConfig: ModuleConfig, sourceFiles: List<Path>): ParsingResponse<Map<DungeonId, Dungeon>> {
  val lexingResults = sourceFiles
      .map { path ->
        val code = Files.readString(path, StandardCharsets.UTF_8)!!
        val (tokens, lexingErrors) = tokenizeAndSanitize(root.relativize(path).toString(), code)
        val (tokenizedGraph, tokenGraphErrors) = toTokenGraph(path, tokens)
        Pair(Pair(path, tokenizedGraph), lexingErrors + tokenGraphErrors)
      }
  val tokenGraphs = lexingResults.associate { it.first }
  val lexingErrors = lexingResults.flatMap { it.second }

  val importMap = tokenGraphs.mapValues { it.value.imports }
  val dungeons = if (moduleConfig.fileNamespaces)
    tokenGraphs
        .mapValues { (path, tokenGraph) ->
          val definitions = tokenGraph.definitions
              .associateBy { PathKey(path.toString(), it.symbol.value) }

          parseDungeon(context, importMap, definitions)
        }
        .mapKeys { it.key.fileName.toString().split(".").first() }
  else {
    val definitions = tokenGraphs.entries
        .flatMap { (path, tokenGraph) ->
          val namespace = root.relativize(path)
              .parent
              .toString()
              .split(Regex("""[/\\]"""))
              .drop(1)
              .joinToString(".")

          tokenGraph.definitions
              .map {
                PathKey(namespace, it.symbol.value) to it
              }
        }
        .associate { it }

    val dungeon = parseDungeon(context, importMap, definitions)
    mapOf(moduleName to dungeon)
  }

  return ParsingResponse(
      dungeons.mapValues { it.value.value },
      lexingErrors + dungeons.values.flatMap { it.errors }
  )
}

fun loadModuleInfo(path: Path): ModuleInfo {
  return ModuleInfo(
      path = path,
      config = loadYamlFile<ModuleConfig>(path.resolve(moduleFileName)) ?: ModuleConfig(),
      sourceFiles = glob("src/**/*.imp", path)
  )
}

fun loadModule(name: String, context: Context, info: ModuleInfo): ParsingResponse<Module> {
  val path = info.path
  val config = info.config
  val sourceFiles = info.sourceFiles
  val (dungeons, errors) = loadSourceFiles(name, path, context, config, sourceFiles)
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

tailrec fun loadModules(root: URI, infos: Map<ModuleId, ModuleInfo>, context: Context, accumulator: Map<ModuleId, ParsingResponse<Module>>): Map<ModuleId, ParsingResponse<Module>> =
    if (infos.none())
      accumulator
    else {
      val (name, info) = infos.entries.first()
      val response = loadModule(name, context, info)
      val (module, _) = response
      val newContext = context + module.dungeons.map { it.value.graph }
      loadModules(root, infos - name, newContext, accumulator + (name to response))
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
                ModuleDependency(
                    dependent = name,
                    provider = provider
                )
              }
        }
        .toSet()

    val (arrangedModules, dependencyErrors) = arrangeDependencies(infos.keys, dependencies)
    val initialContext = listOf(library.namespace)
    val arrangedMap = arrangedModules.associateWith { infos[it]!! }
    val loadingResponse = loadModules(root.toUri(), arrangedMap, initialContext, mapOf())
    val modules = loadingResponse
        .mapValues { it.value.value }

    return CampaignResponse(
        value = Workspace(
            modules = modules,
            dependencies = dependencies
        ),
        campaignErrors = dependencyErrors.map { CampaignError(it) },
        parsingErrors = loadingResponse.flatMap { it.value.errors }
    )
  }
}
