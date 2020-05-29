package campaign

import org.junit.jupiter.api.Test
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.library.standard.standardLibrary
import java.nio.file.Paths

class CampaignTest {
  @Test
  fun canloadAndExecuteWorkspaces() {
    val library = standardLibrary()
    val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("project1/workspace.yaml")!!
    val (workspace, errors) = loadWorkspace(library, workspaceUrl.toURI())
  }
}
