package trnqilo.telecomando.ui.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import trnqilo.telecomando.terminal.TerminalCursor

class TerminalCursorRenderingTest {
  @Test
  fun stylesTheTerminalCellAtTheCursorOffset() {
    val cursorColor = Color.White
    val contentColor = Color.Black

    val rendered = renderTerminalCursor(
      output = AnnotatedString("abc "),
      cursor = TerminalCursor(offset = 3, visible = true),
      cursorColor = cursorColor,
      cursorContentColor = contentColor,
    )

    val cursorStyle = rendered.spanStyles.last()
    assertEquals(3, cursorStyle.start)
    assertEquals(4, cursorStyle.end)
    assertEquals(cursorColor, cursorStyle.item.background)
    assertEquals(contentColor, cursorStyle.item.color)
  }

  @Test
  fun leavesOutputUnchangedWhenCursorIsHidden() {
    val output = AnnotatedString("abc ")

    val rendered = renderTerminalCursor(
      output = output,
      cursor = TerminalCursor(offset = 3, visible = false),
      cursorColor = Color.White,
      cursorContentColor = Color.Black,
    )

    assertSame(output, rendered)
  }

  @Test
  fun leavesOutputUnchangedWhenCursorFallsOutsideTheRenderedGrid() {
    val output = AnnotatedString("")

    val rendered = renderTerminalCursor(
      output = output,
      cursor = TerminalCursor(offset = 0, visible = true),
      cursorColor = Color.White,
      cursorContentColor = Color.Black,
    )

    assertSame(output, rendered)
  }
}
