package trnqilo.telecomando.ui.terminal

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.ssh.SshConnectionRequest
import trnqilo.telecomando.terminal.JschTerminalSessionFactory
import trnqilo.telecomando.terminal.TerminalConnectionRequest
import trnqilo.telecomando.terminal.TerminalCursor
import trnqilo.telecomando.terminal.TerminalSessionFactory
import trnqilo.telecomando.terminal.TerminalStateHolder
import trnqilo.telecomando.terminal.TerminalStatus
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.FormSection

@Composable
fun TerminalScreen(
  server: ServerEntity,
  onBack: () -> Unit,
  sessionFactory: TerminalSessionFactory? = null,
) {
  val coroutineScope = rememberCoroutineScope()
  val resolvedSessionFactory = sessionFactory ?: remember { JschTerminalSessionFactory() }
  val stateHolder = remember(server.serverId, resolvedSessionFactory) {
    TerminalStateHolder(coroutineScope, resolvedSessionFactory)
  }
  val state by stateHolder.state.collectAsState()
  val connectionRequest = remember(server.serverId) { server.toTerminalConnectionRequest() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val terminalFocusRequester = remember { FocusRequester() }
  val inputFocusRequester = remember { FocusRequester() }
  var terminalInput by remember(server.serverId) { mutableStateOf(TextFieldValue()) }
  var ctrlArmed by rememberSaveable(server.serverId) { mutableStateOf(false) }
  var altArmed by rememberSaveable(server.serverId) { mutableStateOf(false) }
  var terminalSize by remember { mutableStateOf(IntSize.Zero) }
  val outputScrollState = rememberScrollState()
  val screenScrollState = rememberScrollState()
  val density = LocalDensity.current
  val bodyMediumStyle = MaterialTheme.typography.bodyMedium
  val bodyMediumFontSize = bodyMediumStyle.fontSize

  fun applyModifiers(sequence: String): String {
    if (sequence.isEmpty()) return sequence
    val payload = applyTerminalModifiers(
      sequence = sequence,
      ctrlArmed = ctrlArmed,
      altArmed = altArmed,
    )
    if (ctrlArmed) {
      ctrlArmed = false
    }
    if (altArmed) {
      altArmed = false
    }
    return payload
  }

  fun sendInput(sequence: String, refocus: Boolean) {
    if (state.status != TerminalStatus.Connected) return
    val payload = applyModifiers(sequence)
    if (payload.isNotEmpty()) {
      stateHolder.sendInput(payload)
    }
    if (refocus) {
      inputFocusRequester.requestFocus()
      keyboardController?.show()
    }
  }

  fun sendSequence(sequence: String) = sendInput(sequence, refocus = true)

  fun sendTypedInput(sequence: String) = sendInput(sequence, refocus = false)

  LaunchedEffect(server.serverId) {
    stateHolder.connect(connectionRequest)
  }

  LaunchedEffect(state.status) {
    if (state.status == TerminalStatus.Connected) {
      inputFocusRequester.requestFocus()
      keyboardController?.show()
    }
  }

  val submitEnter: () -> Unit = {
    if (state.status == TerminalStatus.Connected) {
      sendSequence("\r")
      terminalInput = TextFieldValue()
    }
  }

  LaunchedEffect(state.renderedOutput.text, state.cursor?.offset) {
    if (outputScrollState.maxValue > 0) {
      outputScrollState.scrollTo(outputScrollState.maxValue)
    }
  }

  LaunchedEffect(terminalSize) {
    if (terminalSize.width > 0 && terminalSize.height > 0) {
      val charWidthPx = with(density) { bodyMediumFontSize.toPx() * 0.60f }
      val lineHeightPx = with(density) { bodyMediumFontSize.toPx() * 1.35f }
      val columns = (terminalSize.width / charWidthPx).toInt().coerceAtLeast(40)
      val rows = (terminalSize.height / lineHeightPx).toInt().coerceAtLeast(12)
      stateHolder.resize(columns, rows)
    }
  }

  AppScaffold(
    title = server.name,
    onBack = {
      stateHolder.disconnect()
      onBack()
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(screenScrollState),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      FormSection("Session") {
        Text(
          state.status.label(),
          style = MaterialTheme.typography.titleMedium,
        )
        when (val status = state.status) {
          TerminalStatus.Connecting -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
          is TerminalStatus.Failed -> Text(
            status.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
          )
          else -> Unit
        }
        OutlinedButton(
          onClick = {
            if (state.status == TerminalStatus.Connected || state.status == TerminalStatus.Connecting) {
              stateHolder.disconnect()
            } else {
              stateHolder.connect(connectionRequest)
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(if (state.status == TerminalStatus.Connected || state.status == TerminalStatus.Connecting) "Disconnect" else "Connect")
        }
      }

      FormSection("Terminal") {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp, max = 480.dp)
            .focusRequester(terminalFocusRequester)
            .focusable()
            .clickable {
              inputFocusRequester.requestFocus()
              keyboardController?.show()
            }
            .onPreviewKeyEvent { event ->
              val sequence = event.nativeKeyEvent.toTerminalSequence()
              if (sequence != null && state.status == TerminalStatus.Connected) {
                sendSequence(sequence)
                true
              } else {
                false
              }
            }
            .onSizeChanged { terminalSize = it },
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          shape = MaterialTheme.shapes.medium,
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .padding(12.dp)
                .padding(bottom = 72.dp)
                .verticalScroll(outputScrollState),
            ) {
              val terminalOutput = if (state.status == TerminalStatus.Connected) {
                renderTerminalCursor(
                  output = state.renderedOutput,
                  cursor = state.cursor,
                  cursorColor = MaterialTheme.colorScheme.onSurface,
                  cursorContentColor = MaterialTheme.colorScheme.surfaceVariant,
                )
              } else {
                state.renderedOutput
              }
              Text(
                text = if (terminalOutput.text.isBlank() && state.status != TerminalStatus.Connected) {
                  AnnotatedString("Tap the terminal and connect to begin.")
                } else {
                  terminalOutput
                },
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
              )
            }

            TerminalActionStrip(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .padding(12.dp),
              ctrlArmed = ctrlArmed,
              altArmed = altArmed,
              onCtrlClick = {
                ctrlArmed = !ctrlArmed
                inputFocusRequester.requestFocus()
                keyboardController?.show()
              },
              onAltClick = {
                altArmed = !altArmed
                inputFocusRequester.requestFocus()
                keyboardController?.show()
              },
              onSend = { sequence ->
                sendSequence(sequence)
              },
            )

            BasicTextField(
              value = terminalInput,
              onValueChange = { newValue ->
                if (state.status != TerminalStatus.Connected) return@BasicTextField
                val payload = terminalPayloadFromEdit(
                  previousValue = terminalInput.text,
                  newValue = newValue.text,
                )
                terminalInput = newValue
                if (payload.isNotEmpty()) {
                  sendTypedInput(payload)
                }
              },
              modifier = Modifier
                .fillMaxSize()
                .alpha(0f)
                .zIndex(0f)
                .testTag("terminalInput")
                .focusRequester(inputFocusRequester),
              textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
              ),
              keyboardActions = KeyboardActions(
                onDone = { submitEnter() },
                onSend = { submitEnter() },
              ),
            )
          }
        }
      }
    }
  }
}

internal fun terminalPayloadFromEdit(previousValue: String, newValue: String): String {
  if (previousValue == newValue) return ""
  val commonPrefixLength = previousValue.zip(newValue)
    .takeWhile { (previous, new) -> previous == new }
    .size
  val maximumSuffixLength = minOf(
    previousValue.length - commonPrefixLength,
    newValue.length - commonPrefixLength,
  )
  var commonSuffixLength = 0
  while (
    commonSuffixLength < maximumSuffixLength &&
    previousValue[previousValue.lastIndex - commonSuffixLength] ==
    newValue[newValue.lastIndex - commonSuffixLength]
  ) {
    commonSuffixLength++
  }
  val deletedCharacters = previousValue.length - commonPrefixLength - commonSuffixLength
  val insertedEnd = newValue.length - commonSuffixLength
  val insertedText = newValue.substring(commonPrefixLength, insertedEnd).replace("\n", "\r")
  return "\u007F".repeat(deletedCharacters) + insertedText
}

internal fun applyTerminalModifiers(
  sequence: String,
  ctrlArmed: Boolean,
  altArmed: Boolean,
): String {
  if (sequence.isEmpty()) return sequence
  val controlSequence = if (ctrlArmed) {
    sequence.mapIndexed { index, char ->
      if (index == 0 && char.isLetter()) {
        (char.uppercaseChar().code - 'A'.code + 1).toChar()
      } else {
        char
      }
    }.joinToString(separator = "")
  } else {
    sequence
  }
  return if (altArmed) "\u001B$controlSequence" else controlSequence
}

internal fun renderTerminalCursor(
  output: AnnotatedString,
  cursor: TerminalCursor?,
  cursorColor: androidx.compose.ui.graphics.Color,
  cursorContentColor: androidx.compose.ui.graphics.Color,
): AnnotatedString {
  if (cursor == null || !cursor.visible || cursor.offset !in output.text.indices) return output
  return buildAnnotatedString {
    append(output)
    addStyle(
      style = SpanStyle(
        color = cursorContentColor,
        background = cursorColor,
      ),
      start = cursor.offset,
      end = cursor.offset + 1,
    )
  }
}

private fun TerminalStatus.label(): String = when (this) {
  TerminalStatus.Idle -> "Idle"
  TerminalStatus.Connecting -> "Connecting"
  TerminalStatus.Connected -> "Connected"
  TerminalStatus.Disconnected -> "Disconnected"
  is TerminalStatus.Failed -> "Connection failed: $message"
}

private fun AndroidKeyEvent.toTerminalSequence(): String? = when (action) {
  AndroidKeyEvent.ACTION_UP -> null
  else -> {
    val isCtrlPressed = metaState and AndroidKeyEvent.META_CTRL_ON != 0
    when {
      keyCode == AndroidKeyEvent.KEYCODE_ENTER -> "\r"
      keyCode == AndroidKeyEvent.KEYCODE_TAB -> "\t"
      keyCode == AndroidKeyEvent.KEYCODE_DPAD_UP -> "\u001B[A"
      keyCode == AndroidKeyEvent.KEYCODE_DPAD_DOWN -> "\u001B[B"
      keyCode == AndroidKeyEvent.KEYCODE_DPAD_LEFT -> "\u001B[D"
      keyCode == AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> "\u001B[C"
      keyCode == AndroidKeyEvent.KEYCODE_DEL -> "\u007F"
      keyCode == AndroidKeyEvent.KEYCODE_ESCAPE -> "\u001B"
      isCtrlPressed && unicodeChar > 0 -> {
        val char = unicodeChar.toChar()
        if (char.isLetter()) {
          ((char.uppercaseChar().code - 'A'.code + 1).toChar()).toString()
        } else {
          when (keyCode) {
            AndroidKeyEvent.KEYCODE_C -> "\u0003"
            AndroidKeyEvent.KEYCODE_D -> "\u0004"
            else -> null
          }
        }
      }
      !isCtrlPressed && unicodeChar > 0 -> unicodeChar.toChar().toString()
      else -> when (keyCode) {
        AndroidKeyEvent.KEYCODE_C -> "\u0003"
        AndroidKeyEvent.KEYCODE_D -> "\u0004"
        else -> null
      }
    }
  }
}

@Composable
private fun TerminalActionStrip(
  modifier: Modifier = Modifier,
  ctrlArmed: Boolean,
  altArmed: Boolean,
  onCtrlClick: () -> Unit,
  onAltClick: () -> Unit,
  onSend: (String) -> Unit,
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
    tonalElevation = 1.dp,
    shape = MaterialTheme.shapes.medium,
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
      val buttonWidth = (maxWidth - (6.dp * 7)) / 8
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        TerminalActionButton(text = "Esc", modifier = Modifier.width(buttonWidth), onClick = { onSend("\u001B") })
        TerminalActionButton(text = "Tab", modifier = Modifier.width(buttonWidth), onClick = { onSend("\t") })
        TerminalActionButton(text = "↑", modifier = Modifier.width(buttonWidth), onClick = { onSend("\u001B[A") })
        TerminalActionButton(text = "↓", modifier = Modifier.width(buttonWidth), onClick = { onSend("\u001B[B") })
        TerminalActionButton(text = "←", modifier = Modifier.width(buttonWidth), onClick = { onSend("\u001B[D") })
        TerminalActionButton(text = "→", modifier = Modifier.width(buttonWidth), onClick = { onSend("\u001B[C") })
        TerminalActionButton(
          text = "Alt",
          selected = altArmed,
          modifier = Modifier.width(buttonWidth),
          onClick = onAltClick,
        )
        TerminalActionButton(
          text = "Ctrl",
          selected = ctrlArmed,
          modifier = Modifier.width(buttonWidth),
          onClick = onCtrlClick,
        )
      }
    }
  }
}

@Composable
private fun TerminalActionButton(
  text: String,
  selected: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val containerColor = if (selected) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
  } else {
    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
  }
  val contentColor = if (selected) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurface
  }
  Surface(
    modifier = modifier
      .heightIn(min = 40.dp)
      .clickable(onClick = onClick),
    color = containerColor,
    contentColor = contentColor,
    tonalElevation = 0.dp,
    shape = MaterialTheme.shapes.small,
  ) {
    Box(
      modifier = Modifier.padding(vertical = 10.dp),
    ) {
      Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelMedium,
      )
    }
  }
}

private fun ServerEntity.toTerminalConnectionRequest() = TerminalConnectionRequest(
  title = name,
  connection = SshConnectionRequest(
    destination = address,
    port = port,
    user = username,
    password = password,
  ),
)
