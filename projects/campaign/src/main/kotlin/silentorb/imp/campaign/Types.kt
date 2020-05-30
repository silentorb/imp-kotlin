package silentorb.imp.campaign

import silentorb.imp.core.Dungeon
import silentorb.imp.parsing.general.ParsingErrors
import java.nio.file.Path

typealias ModuleId = String
typealias DungeonId = String

data class Module(
    val dungeons: Map<DungeonId, Dungeon>,
    val fileNamespaces: Boolean
)

val emptyModule = Module(
    dungeons = mapOf(),
    fileNamespaces = false
)

data class Dependency(
    val dependent: ModuleId,
    val provider: ModuleId
)

data class Workspace(
    val modules: Map<ModuleId, Module>,
    val dependencies: Set<Dependency>
)

val emptyWorkspace = Workspace(
    modules = mapOf(),
    dependencies = setOf()
)

data class ModuleConfig(
    val dependencies: List<String> = listOf(),
    val fileNamespaces: Boolean = false
)

data class WorkspaceConfig(
    val modules: List<String> = listOf() // Module directory patterns
)

data class CampaignError(
    val message: Any,
    val arguments: List<Any> = listOf()
)

typealias CampaignErrors = List<CampaignError>

data class CampaignResponse<T>(
    val value: T,
    val campaignErrors: CampaignErrors,
    val parsingErrors: ParsingErrors
)

data class ModuleInfo(
    val config: ModuleConfig,
    val sourceFiles: List<Path>
)
