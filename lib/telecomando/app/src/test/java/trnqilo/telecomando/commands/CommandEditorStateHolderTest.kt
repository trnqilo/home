package trnqilo.telecomando.commands

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandEditorStateHolderTest {
  @Test
  fun updatesFieldsAndMaintainsUniqueServerOrder() {
    val stateHolder = CommandEditorStateHolder()

    stateHolder.updateName("Status")
    stateHolder.updateCommand("uptime")
    stateHolder.addServer(2)
    stateHolder.addServer(1)
    stateHolder.addServer(2)

    assertEquals(
      CommandDraft("Status", "uptime", listOf(2, 1)),
      stateHolder.draft.value,
    )
  }

  @Test
  fun removesSelectedServer() {
    val stateHolder = CommandEditorStateHolder(CommandDraft(selectedServerIds = listOf(2, 1)))

    stateHolder.removeServer(2)

    assertEquals(listOf(1), stateHolder.draft.value.selectedServerIds)
  }

  @Test
  fun changingTypeClearsIncompatibleConnections() {
    val stateHolder = CommandEditorStateHolder(CommandDraft(selectedServerIds = listOf(2, 1)))

    stateHolder.updateType("HTTP")

    assertEquals(emptyList<Int>(), stateHolder.draft.value.selectedServerIds)
  }
}
