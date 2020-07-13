package silentorb.imp.campaign

import silentorb.imp.core.*
import silentorb.imp.execution.Library
import silentorb.imp.execution.mergeImplementationFunctions
import silentorb.imp.parsing.general.GetCode
import silentorb.imp.core.Response
import silentorb.imp.parsing.parser.parseDungeon
import silentorb.imp.parsing.parser.tokenizeAndSanitize
import silentorb.imp.parsing.syntax.toTokenGraph
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val workspaceFileName = "workspace.yaml"
val workspaceFilePath = Paths.get(workspaceFileName)
const val moduleFileName = "module.yaml"
val moduleFilePath = Paths.get(moduleFileName)

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

fun loadModule(getCode: GetCode, name: String, context: Context, info: ModuleInfo): Response<Module> {
  val path = info.path
  val config = info.config
  val sourceFiles = info.sourceFiles
  val (dungeons, errors) = loadSourceFiles(getCode, name, path, context, config, sourceFiles)
  return Response(
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

tailrec fun loadModules(getCode: GetCode, root: URI, infos: Map<ModuleId, ModuleInfo>, context: Context, accumulator: Map<ModuleId, Response<Module>>): Map<ModuleId, Response<Module>> =
    if (infos.none())
      accumulator
    else {
      val (name, info) = infos.entries.first()
      val response = loadModule(getCode, name, context, info)
      val (module, _) = response
      val newContext = context + module.dungeons.map { it.value.namespace }
      loadModules(getCode, root, infos - name, newContext, accumulator + (name to response))
    }

fun loadWorkspace(getCode: GetCode, library: Library, root: Path): Response<Workspace> {
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

    val (arrangedModules, dependencyErrors) = arrangeDependencies(infos.keys, dependencies)
    val initialContext = listOf(library.namespace)
    val arrangedMap = arrangedModules.associateWith { infos[it]!! }
    val loadingResponse = loadModules(getCode, root.toUri(), arrangedMap, initialContext, mapOf())
    val modules = loadingResponse
        .mapValues { it.value.value }

    return Response(
        value = Workspace(
            path = root,
            modules = modules,
            dependencies = dependencies
        ),
        errors = dependencyErrors.map { ImpError(it) } + loadingResponse.flatMap { it.value.errors }
    )
  }
}

fun loadContainingWorkspace(getCode: GetCode, library: Library, root: Path): Response<Workspace>? {
  val moduleDirectory = findContainingModule(root)
  return if (moduleDirectory == null)
    null
  else {
    val workspaceDirectory = findContainingWorkspaceDirectory(moduleDirectory)
    if (workspaceDirectory != null)
      loadWorkspace(getCode, library, workspaceDirectory)
    else
      null
  }
}

fun getModulesContext(modules: Map<ModuleId, Module>): Context {
  val dungeons = modules.map { it.value.dungeons }.reduce { a, b -> a + b }
  return dungeons.values.map { it.namespace }
}

fun getModulesExecutionArtifacts(implementation: FunctionImplementationMap, baseContext: Context, modules: Map<ModuleId, Module>): Pair<Context, FunctionImplementationMap> {
  val context = listOf(mergeNamespaces(baseContext + getModulesContext(modules)))
  val functionGraphs = modules.values
      .map { module ->
        module.dungeons.map {
          it.value.implementationGraphs
        }
            .reduce { a, b -> a + b }
      }
      .reduce { a, b -> a + b }

  val functions = mergeImplementationFunctions(context, functionGraphs)
  return Pair(context, functions)
}
