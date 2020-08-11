package silentorb.imp.campaign

import silentorb.imp.core.*
import silentorb.imp.parsing.general.GetCode
import silentorb.imp.core.Response
import silentorb.imp.parsing.resolution.parseDungeon
import silentorb.imp.parsing.parser.tokenizeAndSanitize
import silentorb.imp.parsing.syntax.toTokenGraph
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val workspaceFileName = "workspace.yaml"
val workspaceFilePath: Path = Paths.get(workspaceFileName)
const val moduleFileName = "module.yaml"
val moduleFilePath: Path = Paths.get(moduleFileName)

fun namespaceFromPath(root: Path, path: Path): String =
    root.relativize(path)
        .toString()
        .split(Regex("""[/\\]"""))
        .drop(1)
        .joinToString(".")

val codeFromFile: GetCode = { path ->
  Files.readString(path, StandardCharsets.UTF_8)
}

fun loadSourceFiles(getCode: GetCode, moduleName: String, root: Path, context: Context, moduleConfig: ModuleConfig, sourceFiles: List<Path>): Response<Map<DungeonId, Dungeon>> {
  val lexingResults = sourceFiles
      .map { path ->
        val code = getCode(path)?.replace("\r\n", "\n") ?: ""
        val (tokens, lexingErrors) = tokenizeAndSanitize(pathToString(root), code)
        val (tokenizedGraph, tokenGraphErrors) = toTokenGraph(pathToString(path), tokens)
        Pair(Pair(path, tokenizedGraph), lexingErrors + tokenGraphErrors)
      }
  val tokenGraphs = lexingResults.associate { it.first }
  val lexingErrors = lexingResults.flatMap { it.second }

  val importMap = tokenGraphs.mapValues { it.value.imports }
  val definitions = tokenGraphs.entries
      .flatMap { (path, tokenGraph) ->
        val namespace = namespaceFromPath(root, path.parent)

        tokenGraph.definitions
            .map {
              PathKey(namespace, it.symbol.value as String) to it
            }
      }
      .associate { it }

  val dungeon = parseDungeon(context, importMap, definitions)
  val dungeons = mapOf(moduleName to dungeon)

  return Response(
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

typealias GetModule = (Context, ModuleId, ModuleInfo) -> Response<Module>

fun loadModule(getCode: GetCode): GetModule = { context, name, info ->
  val path = info.path
  val config = info.config
  val sourceFiles = info.sourceFiles
  val (dungeons, errors) = loadSourceFiles(getCode, name, path, context, config, sourceFiles)
  Response(
      Module(
          path = path,
          dungeons = dungeons
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

tailrec fun loadModules(getModule: GetModule, root: URI, infos: List<Pair<ModuleId, ModuleInfo>>, context: Context, accumulator: Map<ModuleId, Response<Module>> = mapOf()): Response<Map<ModuleId, Module>> =
    if (infos.none()) {
      val modules = accumulator
          .mapValues { it.value.value }
      Response(
          value = modules,
          errors = accumulator.flatMap { it.value.errors }
      )
    } else {
      val (name, info) = infos.first()
      val response = getModule(context, name, info)
      val (module, _) = response
      val newContext = context + module.dungeons.map { it.value.namespace }
      loadModules(getModule, root, infos.drop(1), newContext, accumulator + (name to response))
    }

fun loadAllModules(
    workspace: Workspace,
    context: Context,
    getCode: GetCode): Response<ModuleMap> {
  val (arrangedModules, dependencyErrors) = arrangeDependencies(workspace.modules.keys, workspace.dependencies)
  val arrangedMap = arrangedModules.map { it to workspace.modules[it]!! }
  val loadingResponse = loadModules(loadModule(getCode), workspace.path.toUri(), arrangedMap, context)

  return loadingResponse.copy(
      errors = dependencyErrors.map { ImpError(it) } + loadingResponse.errors
  )
}

fun loadWorkspace(root: Path): Response<Workspace> {
  val workspaceFilePath = root.resolve(workspaceFileName)
  val workspaceConfig = loadYamlFile<WorkspaceConfig>(workspaceFilePath)
  return if (workspaceConfig == null)
    Response(
        value = emptyWorkspace,
        errors = listOf(ImpError(CampaignText.fileNotFound, arguments = listOf(workspaceFilePath)))
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

    return Response(
        value = Workspace(
            path = root,
            modules = infos,
            dependencies = dependencies
        ),
        errors = listOf()
    )
  }
}

fun getContainingWorkspaceDirectory(root: Path): Path? {
  val moduleDirectory = getContainingModule(root)
  return if (moduleDirectory == null)
    null
  else
    findContainingWorkspaceDirectory(moduleDirectory)
}

fun loadContainingWorkspace(getCode: GetCode, context: Context, root: Path): Response<Workspace>? {
  val workspaceDirectory = getContainingWorkspaceDirectory(root)
  return if (workspaceDirectory != null)
    loadWorkspace(workspaceDirectory)
  else
    null
}

fun getModulesContext(modules: Map<ModuleId, Module>): Context {
  val dungeons = modules.map { it.value.dungeons }.reduce { a, b -> a + b }
  return dungeons.values.map { it.namespace }
}

fun getModulesExecutionArtifacts(baseContext: Context, modules: Map<ModuleId, Module>): Context =
    listOf(mergeNamespaces(baseContext + getModulesContext(modules)))
