package com.lecturevault.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object SettingsKeys {
    val THEME = stringPreferencesKey("theme") // system | light | dark
    val ONBOARDED = booleanPreferencesKey("onboarded")
    val CAMERA_QUALITY = stringPreferencesKey("camera_quality") // high | medium | low
}

class SettingsRepository(private val context: Context) {
    val theme = context.dataStore.data.map { it[SettingsKeys.THEME] ?: "system" }
    val onboarded = context.dataStore.data.map { it[SettingsKeys.ONBOARDED] ?: false }
    val cameraQuality = context.dataStore.data.map { it[SettingsKeys.CAMERA_QUALITY] ?: "high" }

    suspend fun setTheme(value: String) { context.dataStore.edit { it[SettingsKeys.THEME] = value } }
    suspend fun setOnboarded(value: Boolean) { context.dataStore.edit { it[SettingsKeys.ONBOARDED] = value } }
    suspend fun setCameraQuality(value: String) { context.dataStore.edit { it[SettingsKeys.CAMERA_QUALITY] = value } }
}
