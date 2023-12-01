package trnqilo.telecomando.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainAppBarTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun opensSettingsFromAction() {
    var opened = false
    composeRule.setContent {
      MaterialTheme {
        MainAppBar(
          onSettings = { opened = true },
        )
      }
    }

    composeRule.onNodeWithContentDescription("Settings").performClick()

    assertEquals(true, opened)
  }
}
