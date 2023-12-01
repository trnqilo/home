package trnqilo.telecomando

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import trnqilo.telecomando.automation.AlarmAutomationScheduler
import trnqilo.telecomando.data.AppRepo
import trnqilo.telecomando.data.DatabaseBuilder.appDatabase
import trnqilo.telecomando.preferences.DataStoreThemePreferences
import trnqilo.telecomando.preferences.ThemeMode
import trnqilo.telecomando.ui.App
import trnqilo.telecomando.ui.AppViewModel
import trnqilo.telecomando.ui.AppViewModel.Factory
import trnqilo.telecomando.ui.theme.TelecomandoTheme
import trnqilo.telecomando.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appVersionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
    val repo = AppRepo(appDatabase(this).commandDao())
    val automationScheduler = AlarmAutomationScheduler(this, repo)
    val appViewModel = ViewModelProvider(
      this,
      Factory(repo, automationScheduler),
    )[AppViewModel::class.java]
    val themeViewModel = ViewModelProvider(
      this,
      ThemeViewModel.Factory(DataStoreThemePreferences(this)),
    )[ThemeViewModel::class.java]
    setContent {
      val themeMode by themeViewModel.mode.collectAsState()
      TelecomandoTheme(themeMode) {
        Content(
          appVersionName = appVersionName,
          themeMode = themeMode,
          onThemeModeChange = themeViewModel::setMode,
          appViewModel = appViewModel,
        )
      }
    }
  }

  @Composable
  private fun Content(
    appVersionName: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    appViewModel: AppViewModel,
  ) {
    App(
      appViewModel = appViewModel,
      appVersionName = appVersionName,
      themeMode = themeMode,
      onThemeModeChange = onThemeModeChange,
    )
  }

}
