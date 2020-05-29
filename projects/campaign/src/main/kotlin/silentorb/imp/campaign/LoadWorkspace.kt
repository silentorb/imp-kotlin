package silentorb.imp.campaign

import silentorb.imp.execution.Library
import java.nio.file.FileSystems
import java.nio.file.Path

fun loadModule(library: Library, path: String): Module {
  return emptyModule
}

fun loadModules(library: Library, origin: Path, globPattern: String): Map<ModuleId, Module> {
  val matcher = FileSystems.getDefault().getPathMatcher(globPattern)
  matcher.matches(origin)

}

fun loadWorkspace(library: Library, workspaceFilePath: Path): CampaignResponse<Workspace> {
  val workspaceConfig = loadYamlFile<WorkspaceConfig>(workspaceFilePath)
  return if (workspaceConfig == null)
    CampaignResponse(emptyWorkspace, listOf(CampaignError(CampaignText.fileNotFound, arguments = listOf(workspaceFilePath))))
  else {
    val modules = workspaceConfig.modules
        .map { loadModules(library, workspaceFilePath.parent, it) }
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
