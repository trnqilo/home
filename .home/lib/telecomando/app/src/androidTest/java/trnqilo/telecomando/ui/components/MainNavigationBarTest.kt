package trnqilo.telecomando.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainNavigationBarTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun navigatesToSelectedDestination() {
    var route: String? = null
    composeRule.setContent {
      MaterialTheme {
        MainNavigationBar(selectedRoute = "commands", onNavigate = { route = it })
      }
    }

    composeRule.onNodeWithText("Connections").performClick()

    assertEquals("connections", route)
  }

  @Test
  fun exposesAutomationsDestination() {
    var route: String? = null
    composeRule.setContent {
      MaterialTheme {
        MainNavigationBar(selectedRoute = "connections", onNavigate = { route = it })
      }
    }

    composeRule.onNodeWithText("Automations").performClick()

    assertEquals("automations", route)
  }
}
