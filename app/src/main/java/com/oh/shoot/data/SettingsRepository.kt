package com.oh.shoot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oh.shoot.domain.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "oh_shoot_settings"
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            cameraFacingFront = prefs[Keys.CAMERA_FACING_FRONT] ?: true,
            mirrorPreview = prefs[Keys.MIRROR_PREVIEW] ?: true,
            contrastBoost = prefs[Keys.CONTRAST_BOOST] ?: 1.4f,
            paperWidth80mm = prefs[Keys.PAPER_WIDTH_80MM] ?: true,
            autoCut = prefs[Keys.AUTO_CUT] ?: true,
            defaultCopies = prefs[Keys.DEFAULT_COPIES] ?: 1,
            headerText = prefs[Keys.HEADER_TEXT] ?: "OH Shoot!",
            footerText = prefs[Keys.FOOTER_TEXT] ?: "ohshoot.ph",
            useBluetoothPrinter = prefs[Keys.USE_BLUETOOTH_PRINTER] ?: true,
            businessMode = prefs[Keys.BUSINESS_MODE] ?: "Rental",
            soundsEnabled = prefs[Keys.SOUNDS_ENABLED] ?: true,
            customLogoUri = prefs[Keys.CUSTOM_LOGO_URI]
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.CAMERA_FACING_FRONT] = settings.cameraFacingFront
            prefs[Keys.MIRROR_PREVIEW] = settings.mirrorPreview
            prefs[Keys.CONTRAST_BOOST] = settings.contrastBoost
            prefs[Keys.PAPER_WIDTH_80MM] = settings.paperWidth80mm
            prefs[Keys.AUTO_CUT] = settings.autoCut
            prefs[Keys.DEFAULT_COPIES] = settings.defaultCopies
            prefs[Keys.HEADER_TEXT] = settings.headerText
            prefs[Keys.FOOTER_TEXT] = settings.footerText
            prefs[Keys.USE_BLUETOOTH_PRINTER] = settings.useBluetoothPrinter
            prefs[Keys.BUSINESS_MODE] = settings.businessMode
            prefs[Keys.SOUNDS_ENABLED] = settings.soundsEnabled
            if (settings.customLogoUri.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_LOGO_URI)
            } else {
                prefs[Keys.CUSTOM_LOGO_URI] = settings.customLogoUri
            }
        }
    }

    private object Keys {
        val CAMERA_FACING_FRONT = booleanPreferencesKey("camera_facing_front")
        val MIRROR_PREVIEW = booleanPreferencesKey("mirror_preview")
        val CONTRAST_BOOST = floatPreferencesKey("contrast_boost")
        val PAPER_WIDTH_80MM = booleanPreferencesKey("paper_width_80mm")
        val AUTO_CUT = booleanPreferencesKey("auto_cut")
        val DEFAULT_COPIES = intPreferencesKey("default_copies")
        val HEADER_TEXT = stringPreferencesKey("header_text")
        val FOOTER_TEXT = stringPreferencesKey("footer_text")
        val USE_BLUETOOTH_PRINTER = booleanPreferencesKey("use_bluetooth_printer")
        val BUSINESS_MODE = stringPreferencesKey("business_mode")
        val SOUNDS_ENABLED = booleanPreferencesKey("sounds_enabled")
        val CUSTOM_LOGO_URI = stringPreferencesKey("custom_logo_uri")
    }
}
