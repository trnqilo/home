package trnqilo.telecomando.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import trnqilo.telecomando.preferences.ThemeMode

class SettingsScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun changesThemeModeFromAppearanceSection() {
    var selectedMode = ThemeMode.System
    composeRule.setContent {
      MaterialTheme {
        SettingsScreen(
          themeMode = selectedMode,
          onThemeModeChange = { selectedMode = it },
          appVersionName = "1.0",
          onBack = {},
        )
      }
    }

    composeRule.onNodeWithText("Dark theme").performClick()

    assertEquals(ThemeMode.Dark, selectedMode)
  }

  @Test
  fun showsVersionInformation() {
    composeRule.setContent {
      MaterialTheme {
        SettingsScreen(
          themeMode = ThemeMode.System,
          onThemeModeChange = {},
          appVersionName = "1.0",
          onBack = {},
        )
      }
    }

    composeRule.onNodeWithText("Version 1.0").assertExists()
  }
}
