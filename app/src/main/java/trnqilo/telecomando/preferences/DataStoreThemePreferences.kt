package trnqilo.telecomando.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme")

class DataStoreThemePreferences(context: Context) : ThemePreferences {
  private val dataStore = context.applicationContext.themeDataStore

  override val themeMode: Flow<ThemeMode> = dataStore.data
    .catch { error ->
      if (error is IOException) emit(emptyPreferences()) else throw error
    }
    .map { preferences -> preferences[ThemeModeKey].toThemeMode() }

  override suspend fun setThemeMode(mode: ThemeMode) {
    dataStore.edit { preferences -> preferences[ThemeModeKey] = mode.name }
  }

  private fun String?.toThemeMode(): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

  private companion object {
    val ThemeModeKey: Preferences.Key<String> = stringPreferencesKey("mode")
  }
}
