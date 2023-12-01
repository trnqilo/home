package trnqilo.telecomando.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

internal class TerminalRenderer(
  columns: Int = DEFAULT_COLUMNS,
  rows: Int = DEFAULT_ROWS,
  private val historyLimit: Int = DEFAULT_HISTORY_LIMIT,
) {
  private var rawText = StringBuilder()
  private var emulator = TerminalEmulator(columns, rows, historyLimit)

  fun append(text: String): AnnotatedString {
    rawText.append(text)
    emulator.process(text)
    return emulator.render()
  }

  fun resize(columns: Int, rows: Int): AnnotatedString {
    val currentText = rawText.toString()
    emulator = TerminalEmulator(columns, rows, historyLimit)
    emulator.process(currentText)
    return emulator.render()
  }

  fun render(): AnnotatedString = emulator.render()

  fun cursor(): TerminalCursor = emulator.cursor()

  private companion object {
    const val ESC = '\u001B'
    const val DEFAULT_COLUMNS = 80
    const val DEFAULT_ROWS = 24
    const val DEFAULT_HISTORY_LIMIT = 2_000
  }
}

private class TerminalEmulator(
  private var columns: Int,
  private var rows: Int,
  private val historyLimit: Int,
) {
  private var style = TerminalStyle()
  private var cursorRow = 0
  private var cursorColumn = 0
  private var savedCursorRow = 0
  private var savedCursorColumn = 0
  private var savedStyle = style
  private var savedAltCursorRow = 0
  private var savedAltCursorColumn = 0
  private var savedAltStyle = style
  private var savedMainBuffer: TerminalBuffer? = null
  private var alternateScreen = false
  private var cursorVisible = true
  private var escapeBuffer: StringBuilder? = null
  private var mainBuffer = TerminalBuffer(columns, rows, historyLimit)
  private var activeBuffer: TerminalBuffer = mainBuffer

  fun process(text: String) {
    var index = 0
    while (index < text.length) {
      val char = text[index]
      val pending = escapeBuffer
      if (pending != null) {
        pending.append(char)
        if (isEscapeComplete(pending)) {
          handleEscape(pending.toString())
          escapeBuffer = null
        }
        index++
        continue
      }
      when (char) {
        '\u001B' -> escapeBuffer = StringBuilder().append(char)
        '\n' -> newline()
        '\r' -> cursorColumn = 0
        '\b' -> backspace()
        '\t' -> tab()
        '\u0007' -> Unit
        else -> if (!char.isISOControl()) put(char)
      }
      index++
    }
  }

  fun render(): AnnotatedString = buildAnnotatedString {
    val lines = if (alternateScreen) activeBuffer.renderLines() else activeBuffer.scrollback + activeBuffer.renderLines()
    lines.forEachIndexed { index, line ->
      appendStyledLine(line)
      if (index < lines.lastIndex) append('\n')
    }
  }

  fun cursor(): TerminalCursor {
    val scrollbackRows = if (alternateScreen) 0 else activeBuffer.scrollback.size
    val offset = (scrollbackRows + cursorRow) * (columns + 1) + cursorColumn
    return TerminalCursor(offset = offset, visible = cursorVisible)
  }

  private fun handleEscape(sequence: String) {
    when {
      sequence == "\u001B7" -> {
        savedCursorRow = cursorRow
        savedCursorColumn = cursorColumn
        savedStyle = style
      }
      sequence == "\u001B8" -> {
        cursorRow = savedCursorRow
        cursorColumn = savedCursorColumn
        style = savedStyle
      }
      sequence.startsWith("\u001B[") -> handleCsi(sequence)
      sequence.startsWith("\u001B]") -> Unit
    }
  }

  private fun handleCsi(sequence: String) {
    val body = sequence.removePrefix("\u001B[")
    if (body.isEmpty()) return
    val command = body.last()
    val parameters = body.dropLast(1)
    val rawParts = parameters.split(';').filter { it.isNotBlank() }
    val parts = rawParts.map { it.removePrefix("?") }
    when (command) {
      'A' -> cursorRow = (cursorRow - (parts.firstOrNull()?.toIntOrNull() ?: 1)).coerceAtLeast(0)
      'B' -> cursorRow = (cursorRow + (parts.firstOrNull()?.toIntOrNull() ?: 1)).coerceAtMost(rows - 1)
      'C' -> cursorColumn = (cursorColumn + (parts.firstOrNull()?.toIntOrNull() ?: 1)).coerceAtMost(columns - 1)
      'D' -> cursorColumn = (cursorColumn - (parts.firstOrNull()?.toIntOrNull() ?: 1)).coerceAtLeast(0)
      'H', 'f' -> {
        val row = (parts.getOrNull(0)?.toIntOrNull() ?: 1) - 1
        val column = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        cursorRow = row.coerceIn(0, rows - 1)
        cursorColumn = column.coerceIn(0, columns - 1)
      }
      'J' -> when (parts.firstOrNull()?.toIntOrNull() ?: 0) {
        0 -> activeBuffer.clearFrom(cursorRow, cursorColumn)
        1 -> activeBuffer.clearTo(cursorRow, cursorColumn)
        2 -> activeBuffer.clearAll()
      }
      'K' -> when (parts.firstOrNull()?.toIntOrNull() ?: 0) {
        0 -> activeBuffer.clearLineFrom(cursorRow, cursorColumn)
        1 -> activeBuffer.clearLineTo(cursorRow, cursorColumn)
        2 -> activeBuffer.clearLine(cursorRow)
      }
      'm' -> applySgr(parts)
      's' -> {
        savedCursorRow = cursorRow
        savedCursorColumn = cursorColumn
        savedStyle = style
      }
      'u' -> {
        cursorRow = savedCursorRow
        cursorColumn = savedCursorColumn
        style = savedStyle
      }
      'h', 'l' -> {
        val enabled = command == 'h'
        if (rawParts.any { it == "?1049" || it == "1049" }) {
          setAlternateScreen(enabled)
        }
        if (rawParts.any { it == "?25" || it == "25" }) {
          cursorVisible = enabled
        }
      }
    }
  }

  private fun applySgr(parts: List<String>) {
    if (parts.isEmpty()) {
      style = TerminalStyle()
      return
    }
    var index = 0
    while (index < parts.size) {
      when (parts[index].toIntOrNull() ?: 0) {
        0 -> style = TerminalStyle()
        1 -> style = style.copy(bold = true)
        4 -> style = style.copy(underline = true)
        7 -> style = style.copy(inverse = true)
        22 -> style = style.copy(bold = false)
        24 -> style = style.copy(underline = false)
        27 -> style = style.copy(inverse = false)
        39 -> style = style.copy(foreground = null)
        49 -> style = style.copy(background = null)
        in 30..37 -> style = style.copy(foreground = basicColor(parts[index].toInt()))
        in 40..47 -> style = style.copy(background = basicColor(parts[index].toInt() - 10))
        in 90..97 -> style = style.copy(foreground = basicColor(parts[index].toInt() - 60, bright = true))
        in 100..107 -> style = style.copy(background = basicColor(parts[index].toInt() - 70, bright = true))
        38, 48 -> {
          val isForeground = parts[index] == "38"
          val mode = parts.getOrNull(index + 1)?.toIntOrNull()
          when (mode) {
            5 -> {
              val colorIndex = parts.getOrNull(index + 2)?.toIntOrNull()
              if (colorIndex != null) {
                val color = xtermColor(colorIndex)
                style = if (isForeground) style.copy(foreground = color) else style.copy(background = color)
              }
              index += 2
            }
            2 -> {
              val r = parts.getOrNull(index + 2)?.toIntOrNull()
              val g = parts.getOrNull(index + 3)?.toIntOrNull()
              val b = parts.getOrNull(index + 4)?.toIntOrNull()
              if (r != null && g != null && b != null) {
                val color = argb(255, r, g, b)
                style = if (isForeground) style.copy(foreground = color) else style.copy(background = color)
              }
              index += 4
            }
          }
        }
      }
      index++
    }
  }

  private fun setAlternateScreen(enabled: Boolean) {
    if (enabled == alternateScreen) return
    if (enabled) {
      savedAltCursorRow = cursorRow
      savedAltCursorColumn = cursorColumn
      savedAltStyle = style
      savedMainBuffer = mainBuffer
      activeBuffer = TerminalBuffer(columns, rows, historyLimit)
      alternateScreen = true
      cursorRow = 0
      cursorColumn = 0
      return
    }
    alternateScreen = false
    activeBuffer = savedMainBuffer ?: mainBuffer
    cursorRow = savedAltCursorRow
    cursorColumn = savedAltCursorColumn
    style = savedAltStyle
  }

  private fun newline() {
    cursorColumn = 0
    cursorRow++
    if (cursorRow >= rows) {
      activeBuffer.scrollUp()
      cursorRow = rows - 1
    }
  }

  private fun backspace() {
    if (cursorColumn > 0) cursorColumn--
  }

  private fun tab() {
    val nextStop = ((cursorColumn / TAB_WIDTH) + 1) * TAB_WIDTH
    cursorColumn = nextStop.coerceAtMost(columns - 1)
  }

  private fun put(char: Char) {
    activeBuffer.put(cursorRow, cursorColumn, char, style)
    cursorColumn++
    if (cursorColumn >= columns) {
      cursorColumn = 0
      cursorRow++
      if (cursorRow >= rows) {
        activeBuffer.scrollUp()
        cursorRow = rows - 1
      }
    }
  }

  private fun isEscapeComplete(buffer: StringBuilder): Boolean {
    if (buffer.length < 2) return false
    if (buffer[1] == '[') {
      val last = buffer.last()
      return last.isLetter() || last == '~'
    }
    if (buffer[1] == ']') {
      return buffer.last() == '\u0007' || buffer.endsWith(ESCAPE_STRING_TERMINATOR)
    }
    return buffer.length >= 2
  }

  private fun StringBuilder.endsWith(suffix: String): Boolean {
    if (length < suffix.length) return false
    for (i in suffix.indices) {
      if (this[length - suffix.length + i] != suffix[i]) return false
    }
    return true
  }

  private fun AnnotatedString.Builder.appendStyledLine(line: List<TerminalCell>) {
    if (line.isEmpty()) return
    var index = 0
    while (index < line.size) {
      val cell = line[index]
      val start = index
      while (index < line.size && line[index].style == cell.style) {
        index++
      }
      val segment = line.subList(start, index).joinToString(separator = "") { it.char.toString() }
      withStyle(cell.style.toSpanStyle()) {
        append(segment)
      }
    }
  }

  private fun TerminalStyle.toSpanStyle(): SpanStyle {
    val effectiveForeground = if (inverse) background else foreground
    val effectiveBackground = if (inverse) foreground else background
    val fg = effectiveForeground?.let { Color(it.toLong()) }
    val bg = effectiveBackground?.let { Color(it.toLong()) }
    return SpanStyle(
      color = fg ?: Color.Unspecified,
      background = bg ?: Color.Unspecified,
      fontWeight = if (bold) FontWeight.Bold else null,
      textDecoration = if (underline) TextDecoration.Underline else null,
    )
  }

  private fun basicColor(code: Int, bright: Boolean = false): Int = when (code) {
    30 -> if (bright) PALETTE[8] else PALETTE[0]
    31 -> if (bright) PALETTE[9] else PALETTE[1]
    32 -> if (bright) PALETTE[10] else PALETTE[2]
    33 -> if (bright) PALETTE[11] else PALETTE[3]
    34 -> if (bright) PALETTE[12] else PALETTE[4]
    35 -> if (bright) PALETTE[13] else PALETTE[5]
    36 -> if (bright) PALETTE[14] else PALETTE[6]
    37 -> if (bright) PALETTE[15] else PALETTE[7]
    else -> PALETTE[7]
  }

  private fun xtermColor(index: Int): Int = when {
    index in 0..15 -> PALETTE[index]
    index in 16..231 -> {
      val cube = index - 16
      val r = cube / 36
      val g = (cube % 36) / 6
      val b = cube % 6
      val component = fun(value: Int): Int = if (value == 0) 0 else 55 + value * 40
      argb(255, component(r), component(g), component(b))
    }
    index in 232..255 -> {
      val gray = 8 + (index - 232) * 10
      argb(255, gray, gray, gray)
    }
    else -> PALETTE[7]
  }

  private companion object {
    const val TAB_WIDTH = 8
    const val ESCAPE_STRING_TERMINATOR = "\u001B\\"
    val PALETTE = intArrayOf(
      0xFF000000.toInt(),
      0xFFAA0000.toInt(),
      0xFF00AA00.toInt(),
      0xFFAA5500.toInt(),
      0xFF0000AA.toInt(),
      0xFFAA00AA.toInt(),
      0xFF00AAAA.toInt(),
      0xFFAAAAAA.toInt(),
      0xFF555555.toInt(),
      0xFFFF5555.toInt(),
      0xFF55FF55.toInt(),
      0xFFFFFF55.toInt(),
      0xFF5555FF.toInt(),
      0xFFFF55FF.toInt(),
      0xFF55FFFF.toInt(),
      0xFFFFFFFF.toInt(),
    )
  }
}

private data class TerminalBuffer(
  val columns: Int,
  val rows: Int,
  val historyLimit: Int,
  val scrollback: MutableList<List<TerminalCell>> = mutableListOf(),
  val cells: MutableList<MutableList<TerminalCell>> = MutableList(rows) {
    MutableList(columns) { TerminalCell() }
  },
) {
  fun put(row: Int, column: Int, char: Char, style: TerminalStyle) {
    if (row !in 0 until rows || column !in 0 until columns) return
    cells[row][column] = TerminalCell(char, style)
  }

  fun clearFrom(row: Int, column: Int) {
    for (r in row until rows) {
      val start = if (r == row) column else 0
      for (c in start until columns) {
        put(r, c, ' ', TerminalStyle())
      }
    }
  }

  fun clearTo(row: Int, column: Int) {
    for (r in 0..row.coerceAtMost(rows - 1)) {
      val end = if (r == row) column else columns - 1
      for (c in 0..end.coerceAtMost(columns - 1)) {
        put(r, c, ' ', TerminalStyle())
      }
    }
  }

  fun clearAll() {
    for (row in 0 until rows) {
      clearLine(row)
    }
  }

  fun clearLineFrom(row: Int, column: Int) {
    for (c in column until columns) {
      put(row, c, ' ', TerminalStyle())
    }
  }

  fun clearLineTo(row: Int, column: Int) {
    for (c in 0..column.coerceAtMost(columns - 1)) {
      put(row, c, ' ', TerminalStyle())
    }
  }

  fun clearLine(row: Int) {
    for (c in 0 until columns) {
      put(row, c, ' ', TerminalStyle())
    }
  }

  fun scrollUp() {
    if (cells.isEmpty()) return
    scrollback += cells.first().map { it.copy() }
    if (scrollback.size > historyLimit) {
      scrollback.removeAt(0)
    }
    cells.removeAt(0)
    cells.add(MutableList(columns) { TerminalCell() })
  }

  fun renderLines(): List<List<TerminalCell>> = cells
}

private data class TerminalCell(
  val char: Char = ' ',
  val style: TerminalStyle = TerminalStyle(),
)

private data class TerminalStyle(
  val foreground: Int? = null,
  val background: Int? = null,
  val bold: Boolean = false,
  val underline: Boolean = false,
  val inverse: Boolean = false,
)

private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
  (alpha.coerceIn(0, 255) shl 24) or
    (red.coerceIn(0, 255) shl 16) or
    (green.coerceIn(0, 255) shl 8) or
    blue.coerceIn(0, 255)
