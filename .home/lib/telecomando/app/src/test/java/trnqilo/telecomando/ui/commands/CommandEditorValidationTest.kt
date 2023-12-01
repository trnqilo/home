package trnqilo.telecomando.ui.commands

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import trnqilo.telecomando.commands.CommandDraft
import trnqilo.telecomando.data.CommandType

class CommandEditorValidationTest {
  @Test
  fun allowsBodylessHttpCommands() {
    assertTrue(
      canSaveCommand(
        CommandDraft(
          name = "Health",
          type = CommandType.HTTP.name,
          httpPath = "",
        )
      )
    )
  }

  @Test
  fun rejectsMalformedHttpHeaders() {
    assertFalse(
      canSaveCommand(
        CommandDraft(
          name = "Health",
          type = CommandType.HTTP.name,
          httpPath = "https://example.test/health",
          httpHeadersJson = "not json",
        )
      )
    )
  }
}
