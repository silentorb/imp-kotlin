package silentorb.imp.campaign

import silentorb.imp.core.Dungeon

typealias ModuleId = String
typealias DungeonId = String

data class Module(
    val dungeons: Map<DungeonId, Dungeon>
)

data class Dependency(
    val dependent: ModuleId,
    val provider: ModuleId
)

data class Workspace(
    val modules: Map<ModuleId, Module>,
    val dependencies: Set<Dependency>
)

data class ModuleConfig(
    val dependencies: List<String> = listOf(),
    val fileNamespaces: Boolean = false
)

data class WorkspaceConfig(
    val modules: List<String> = listOf() // Module directory patterns
)
