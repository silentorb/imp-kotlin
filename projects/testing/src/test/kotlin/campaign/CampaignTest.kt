package campaign

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import silentorb.imp.campaign.getModulesExecutionArtifacts
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.execution.mergeImplementationFunctions
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.testing.errored
import java.nio.file.Paths

class CampaignTest {
  @Test
  fun canloadAndExecuteWorkspaces() {
    val library = standardLibrary()
    val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("project1/workspace.yaml")!!
    val (workspace, campaignErrors, parsingErrors) = loadWorkspace(library, Paths.get(workspaceUrl.toURI()).parent)
    assertTrue(campaignErrors.none()) { campaignErrors.first().message.toString() }
    errored(parsingErrors)
    val modules = workspace.modules
    assertEquals(2, modules.size)
    assertEquals(2, modules["assets"]!!.dungeons.size)
    assertEquals(1, modules["lib"]!!.dungeons.size)
    val (context, functions) = getModulesExecutionArtifacts(library.implementation, modules)
    val assets = modules["assets"]!!.dungeons
    val mouseValue = executeToSingleValue(context, functions, assets["mouse"]!!)
    val ravenValue = executeToSingleValue(context, functions, assets["raven"]!!)
    assertEquals(11, mouseValue)
    assertEquals(21, ravenValue)
  }
}
