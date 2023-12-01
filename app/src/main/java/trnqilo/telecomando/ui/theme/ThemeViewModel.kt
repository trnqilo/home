package trnqilo.telecomando.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import trnqilo.telecomando.preferences.ThemeMode
import trnqilo.telecomando.preferences.ThemePreferences

class ThemeViewModel(private val preferences: ThemePreferences) : ViewModel() {
  val mode: StateFlow<ThemeMode> = preferences.themeMode.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ThemeMode.System,
  )

  fun setMode(mode: ThemeMode) {
    viewModelScope.launch { preferences.setThemeMode(mode) }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val preferences: ThemePreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      ThemeViewModel(preferences) as T
  }
}
