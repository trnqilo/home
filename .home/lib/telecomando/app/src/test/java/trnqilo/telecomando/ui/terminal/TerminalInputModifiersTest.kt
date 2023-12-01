package trnqilo.telecomando.ui.terminal

import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalInputModifiersTest {
  @Test
  fun emitsAppendedCharacters() {
    assertEquals(" world", terminalPayloadFromEdit("hello", "hello world"))
  }

  @Test
  fun convertsDeletedCharactersToBackspace() {
    assertEquals("\u007F", terminalPayloadFromEdit("hello", "hell"))
  }

  @Test
  fun convertsImeNewlinesToTerminalCarriageReturns() {
    assertEquals("\r", terminalPayloadFromEdit("command", "command\n"))
  }

  @Test
  fun representsReplacementEditsAsDeletionThenInsertion() {
    assertEquals("\u007FH", terminalPayloadFromEdit("hello", "Hello"))
  }

  @Test
  fun convertsCommonCtrlKeysToControlCharacters() {
    assertEquals("\u0003", applyTerminalModifiers("c", ctrlArmed = true, altArmed = false))
    assertEquals("\u0017", applyTerminalModifiers("w", ctrlArmed = true, altArmed = false))
    assertEquals("\u0004", applyTerminalModifiers("d", ctrlArmed = true, altArmed = false))
  }

  @Test
  fun appliesCtrlBeforeAlt() {
    assertEquals(
      "\u001B\u0003",
      applyTerminalModifiers("c", ctrlArmed = true, altArmed = true),
    )
  }

  @Test
  fun onlyAppliesCtrlToTheFirstCharacter() {
    assertEquals(
      "\u0003ontinue",
      applyTerminalModifiers("continue", ctrlArmed = true, altArmed = false),
    )
  }
}
