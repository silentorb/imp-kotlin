package silentorb.imp.campaign

import silentorb.imp.core.Dependency
import silentorb.imp.core.Dungeon
import silentorb.imp.core.ImpErrors
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
    val dependencies: List<String> = listOf()
)

data class WorkspaceConfig(
    val modules: List<String> = listOf() // Module directory patterns
)

data class ModuleInfo(
    val path: Path,
    val config: ModuleConfig,
    val sourceFiles: List<Path>
)
