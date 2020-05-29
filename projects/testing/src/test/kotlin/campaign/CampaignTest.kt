package campaign

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.parsing.general.handleRoot
import silentorb.imp.parsing.parser.parseTextBranching
import silentorb.imp.testing.errored

class ExecutionTest {
  @Test
  fun canExecute() {
    val library = standardLibrary()
    val workspacePath = "get workspace path"
    val (workspace, errors) = loadWorkspace(workspacePath)
  }
}
