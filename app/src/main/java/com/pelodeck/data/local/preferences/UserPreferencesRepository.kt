package com.pelodeck.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pelodeck.domain.model.ThemeSkin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pelodeck_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_SKIN = stringPreferencesKey("selected_skin")
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val PREFERRED_UNITS = stringPreferencesKey("preferred_units") // IMPERIAL or METRIC
        val USE_HARDWARE_TELEMETRY = booleanPreferencesKey("use_hardware_telemetry")
    }

    val selectedSkin: Flow<ThemeSkin> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val skinKey = preferences[PreferencesKeys.SELECTED_SKIN] ?: ThemeSkin.WINAMP.key
            ThemeSkin.fromKey(skinKey)
        }

    val activeProfileId: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROFILE_ID]
        }

    val preferredUnits: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PREFERRED_UNITS] ?: "IMPERIAL"
        }

    val useHardwareTelemetry: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.USE_HARDWARE_TELEMETRY] ?: false
        }

    suspend fun setSelectedSkin(skin: ThemeSkin) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SKIN] = skin.key
        }
    }

    suspend fun setActiveProfileId(profileId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun setPreferredUnits(units: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_UNITS] = units
        }
    }

    suspend fun setUseHardwareTelemetry(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_HARDWARE_TELEMETRY] = enabled
        }
    }
}
