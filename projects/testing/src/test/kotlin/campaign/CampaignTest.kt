package campaign

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import silentorb.imp.campaign.*
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultImpNamespace
import silentorb.imp.core.getGraphOutputNodes
import silentorb.imp.core.mergeNamespaces
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.testing.errored
import silentorb.imp.testing.library.Character
import java.nio.file.Paths

class CampaignTest {
  @Test
  fun canLoadAndExecuteWorkspaces() {
    val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("project1/workspace.yaml")!!
    val (workspace, errors) = loadWorkspace(Paths.get(workspaceUrl.toURI()).parent)
    assertTrue(errors.none()) { errors.first().message.toString() }
    errored(errors)
    val initialContext = listOf(defaultImpNamespace(), standardLibrary()) + loadLibrariesFromJava(workspace)
    val (modules, modulesErrors) = loadAllModules(workspace, initialContext, codeFromFile)
    errored(modulesErrors)
    assertEquals(2, modules.size)
    assertEquals(1, modules["assets"]!!.dungeons.size)
    assertEquals(1, modules["lib"]!!.dungeons.size)
    val context= getModulesExecutionArtifacts(initialContext, modules)
    val outputs = getGraphOutputNodes(mergeNamespaces(context))
        .filter { it.path == "assets" }

    assertEquals(3, outputs.size)
    val mouseValue = executeToSingleValue(context, PathKey("assets", "mouse"))
    val ravenValue = executeToSingleValue(context, PathKey("assets", "raven"))
    val fugueValue = executeToSingleValue(context, PathKey("assets", "fugue"))
    val fugue = fugueValue as? Character

    assertEquals(11, mouseValue)
    assertEquals(21, ravenValue)
    assertTrue(fugue != null)
    assertEquals("Fugue", fugue?.name)
    assertEquals(14, fugue?.intelligence)
  }
}
