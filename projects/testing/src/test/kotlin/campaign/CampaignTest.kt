package campaign

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.library.standard.standardLibrary
import java.nio.file.Paths

class CampaignTest {
  @Test
  fun canloadAndExecuteWorkspaces() {
    val library = standardLibrary()
    val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("project1")!!
    val (workspace, errors) = loadWorkspace(library, Paths.get(workspaceUrl.toURI()))
    assertEquals(2, workspace.modules.size)
    assertEquals(2, workspace.modules["assets"]!!.dungeons.size)
    assertEquals(2, workspace.modules["lib"]!!.dungeons.size)
  }
}
