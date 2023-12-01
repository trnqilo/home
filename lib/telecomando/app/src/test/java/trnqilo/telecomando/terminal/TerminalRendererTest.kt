package trnqilo.telecomando.terminal

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalRendererTest {
  @Test
  fun rendersAnsiForegroundColor() {
    val renderer = TerminalRenderer()
    val rendered = renderer.append("\u001B[31mred\u001B[0m")

    assertEquals("red", rendered.text.trim())
    assertTrue(rendered.spanStyles.isNotEmpty())
    assertEquals(Color(0xFFAA0000.toLong()), rendered.spanStyles.first().item.color)
  }

  @Test
  fun switchesToAlternateScreen() {
    val renderer = TerminalRenderer()
    renderer.append("main\n")
    val rendered = renderer.append("\u001B[?1049halt")

    assertTrue(rendered.text.contains("alt"))
    assertTrue(!rendered.text.contains("main"))
  }

  @Test
  fun tracksCursorOffsetAcrossRowsAndColumns() {
    val renderer = TerminalRenderer(columns = 8, rows = 3)

    renderer.append("abc\r\nxy")

    assertEquals(1 * (8 + 1) + 2, renderer.cursor().offset)
    assertTrue(renderer.cursor().visible)
  }

  @Test
  fun tracksCursorMovedByAnsiSequence() {
    val renderer = TerminalRenderer(columns = 8, rows = 3)

    renderer.append("abc\u001B[2;4H")

    assertEquals(1 * (8 + 1) + 3, renderer.cursor().offset)
  }

  @Test
  fun includesScrollbackInCursorOffset() {
    val renderer = TerminalRenderer(columns = 8, rows = 2)

    renderer.append("one\ntwo\n")

    assertEquals(2 * (8 + 1), renderer.cursor().offset)
  }

  @Test
  fun honorsAnsiCursorVisibility() {
    val renderer = TerminalRenderer(columns = 8, rows = 3)

    renderer.append("\u001B[?25l")
    assertTrue(!renderer.cursor().visible)

    renderer.append("\u001B[?25h")
    assertTrue(renderer.cursor().visible)
  }

  @Test
  fun alternateScreenCursorOffsetDoesNotIncludeMainScrollback() {
    val renderer = TerminalRenderer(columns = 8, rows = 2)
    renderer.append("one\ntwo\n")

    renderer.append("\u001B[?1049h\u001B[2;3H")

    assertEquals(1 * (8 + 1) + 2, renderer.cursor().offset)
  }
}
