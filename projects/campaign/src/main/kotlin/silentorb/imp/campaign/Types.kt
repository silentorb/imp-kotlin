package silentorb.imp.campaign

import silentorb.imp.core.Dependency
import silentorb.imp.core.Dungeon
import java.nio.file.Path
import java.nio.file.Paths

typealias ModuleId = String
typealias DungeonId = String

data class Module(
    val path: Path,
    val dungeons: Map<DungeonId, Dungeon>
)

typealias ModuleDependency = Dependency<ModuleId>

typealias ModuleMap = Map<ModuleId, Module>

data class JavaImport(
    val packagePath: String,
    val classPath: String,
    val method: String
)

data class Workspace(
    val path: Path,
    val modules: Map<ModuleId, ModuleInfo>,
    val dependencies: Set<ModuleDependency>
)

val emptyWorkspace = Workspace(
    path = Paths.get(""),
    modules = mapOf(),
    dependencies = setOf()
)

data class ModuleConfig(
    val dependencies: List<String> = listOf(),
    val javaImports: List<JavaImport> = listOf()
)

typealias PathGlob = String

data class WorkspaceConfig(
    val modules: List<PathGlob> = listOf()
)

data class ModuleInfo(
    val path: Path,
    val config: ModuleConfig,
    val sourceFiles: List<Path>
)
