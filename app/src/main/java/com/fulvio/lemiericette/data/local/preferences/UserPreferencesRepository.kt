package com.fulvio.lemiericette.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fulvio.lemiericette.domain.model.AppFontStyle
import com.fulvio.lemiericette.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository DataStore per il salvataggio ed il recupero delle preferenze dell'utente (Tema, Font, Schermo Sempre Acceso, Impostazioni, Sample Data Flag).
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val appFontStyleKey = stringPreferencesKey("app_font_style")
    private val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")
    private val hasPopulatedSampleDataKey = booleanPreferencesKey("has_populated_sample_data")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[themeModeKey])
    }

    val appFontStyle: Flow<AppFontStyle> = context.dataStore.data.map { prefs ->
        AppFontStyle.fromName(prefs[appFontStyleKey])
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keepScreenOnKey] ?: false
    }

    val hasPopulatedSampleData: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[hasPopulatedSampleDataKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setAppFontStyle(fontStyle: AppFontStyle) {
        context.dataStore.edit { prefs ->
            prefs[appFontStyleKey] = fontStyle.name
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keepScreenOnKey] = enabled
        }
    }

    suspend fun setHasPopulatedSampleData(populated: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[hasPopulatedSampleDataKey] = populated
        }
    }
}
