package trnqilo.telecomando.preferences

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
  System,
  Light,
  Dark,
}

interface ThemePreferences {
  val themeMode: Flow<ThemeMode>

  suspend fun setThemeMode(mode: ThemeMode)
}
