package com.ohshootstudio.resibooth.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ohshootstudio.resibooth.domain.AppSettings
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
        val oldFacingFront = prefs[Keys.CAMERA_FACING_FRONT] ?: true
        val lensFacing = prefs[Keys.CAMERA_LENS_FACING] ?: if (oldFacingFront) 0 else 1
        
        val oldBluetoothSetting = prefs[Keys.USE_BLUETOOTH_PRINTER]
        val connectionType = prefs[Keys.PRINTER_CONNECTION_TYPE] ?: if (oldBluetoothSetting == false) "USB" else "Bluetooth"
        
        AppSettings(
            cameraFacingFront = oldFacingFront,
            cameraLensFacing = lensFacing,
            mirrorPreview = prefs[Keys.MIRROR_PREVIEW] ?: true,
            contrastBoost = prefs[Keys.CONTRAST_BOOST] ?: 1.4f,
            paperWidth80mm = prefs[Keys.PAPER_WIDTH_80MM] ?: true,
            autoCut = prefs[Keys.AUTO_CUT] ?: true,
            defaultCopies = prefs[Keys.DEFAULT_COPIES] ?: 1,
            headerText = prefs[Keys.HEADER_TEXT] ?: "OH Shoot!",
            footerText = prefs[Keys.FOOTER_TEXT] ?: "ohshoot.ph",
            useBluetoothPrinter = oldBluetoothSetting ?: true,
            printerConnectionType = connectionType,
            networkPrinterIp = prefs[Keys.NETWORK_PRINTER_IP] ?: "192.168.1.100",
            networkPrinterPort = prefs[Keys.NETWORK_PRINTER_PORT] ?: 9100,
            businessMode = prefs[Keys.BUSINESS_MODE] ?: "Rental",
            soundsEnabled = prefs[Keys.SOUNDS_ENABLED] ?: true,
            customLogoUri = prefs[Keys.CUSTOM_LOGO_URI],
            printedCornerRadius = prefs[Keys.PRINTED_CORNER_RADIUS] ?: 8f,
            headerIconSizeDp = prefs[Keys.HEADER_ICON_SIZE] ?: 48f,
            standbyImageUri = prefs[Keys.STANDBY_IMAGE_URI],
            continuousCountdown = prefs[Keys.CONTINUOUS_COUNTDOWN] ?: false,
            squareMode = prefs[Keys.SQUARE_MODE] ?: false,
            borderDesignId = prefs[Keys.BORDER_DESIGN_ID] ?: 0,
            ringLightEnabled = prefs[Keys.RING_LIGHT_ENABLED] ?: true,
            saveToDevice = prefs[Keys.SAVE_TO_DEVICE] ?: true,
            themeName = prefs[Keys.THEME_NAME] ?: "Dark",
            startButtonOffsetX = prefs[Keys.START_BUTTON_OFFSET_X] ?: 0f,
            startButtonOffsetY = prefs[Keys.START_BUTTON_OFFSET_Y] ?: 0f,
            startButtonScale = prefs[Keys.START_BUTTON_SCALE] ?: 1f,
            customLayoutTemplate = prefs[Keys.CUSTOM_LAYOUT_TEMPLATE] ?: "",
            isActivated = prefs[Keys.IS_ACTIVATED] ?: false,
            activationKey = prefs[Keys.ACTIVATION_KEY] ?: ""
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.CAMERA_FACING_FRONT] = settings.cameraLensFacing == 0
            prefs[Keys.CAMERA_LENS_FACING] = settings.cameraLensFacing
            prefs[Keys.MIRROR_PREVIEW] = settings.mirrorPreview
            prefs[Keys.CONTRAST_BOOST] = settings.contrastBoost
            prefs[Keys.PAPER_WIDTH_80MM] = settings.paperWidth80mm
            prefs[Keys.AUTO_CUT] = settings.autoCut
            prefs[Keys.DEFAULT_COPIES] = settings.defaultCopies
            prefs[Keys.HEADER_TEXT] = settings.headerText
            prefs[Keys.FOOTER_TEXT] = settings.footerText
            prefs[Keys.USE_BLUETOOTH_PRINTER] = settings.useBluetoothPrinter
            prefs[Keys.PRINTER_CONNECTION_TYPE] = settings.printerConnectionType
            prefs[Keys.NETWORK_PRINTER_IP] = settings.networkPrinterIp
            prefs[Keys.NETWORK_PRINTER_PORT] = settings.networkPrinterPort
            prefs[Keys.BUSINESS_MODE] = settings.businessMode
            prefs[Keys.SOUNDS_ENABLED] = settings.soundsEnabled
            prefs[Keys.PRINTED_CORNER_RADIUS] = settings.printedCornerRadius
            prefs[Keys.HEADER_ICON_SIZE] = settings.headerIconSizeDp
            prefs[Keys.CONTINUOUS_COUNTDOWN] = settings.continuousCountdown
            prefs[Keys.SQUARE_MODE] = settings.squareMode
            prefs[Keys.BORDER_DESIGN_ID] = settings.borderDesignId
            prefs[Keys.RING_LIGHT_ENABLED] = settings.ringLightEnabled
            prefs[Keys.SAVE_TO_DEVICE] = settings.saveToDevice
            prefs[Keys.THEME_NAME] = settings.themeName
            prefs[Keys.START_BUTTON_OFFSET_X] = settings.startButtonOffsetX
            prefs[Keys.START_BUTTON_OFFSET_Y] = settings.startButtonOffsetY
            prefs[Keys.START_BUTTON_SCALE] = settings.startButtonScale
            prefs[Keys.CUSTOM_LAYOUT_TEMPLATE] = settings.customLayoutTemplate
            prefs[Keys.IS_ACTIVATED] = settings.isActivated
            prefs[Keys.ACTIVATION_KEY] = settings.activationKey
            
            if (settings.customLogoUri.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_LOGO_URI)
            } else {
                prefs[Keys.CUSTOM_LOGO_URI] = settings.customLogoUri
            }

            if (settings.standbyImageUri.isNullOrBlank()) {
                prefs.remove(Keys.STANDBY_IMAGE_URI)
            } else {
                prefs[Keys.STANDBY_IMAGE_URI] = settings.standbyImageUri
            }
        }
    }

    private object Keys {
        val CAMERA_FACING_FRONT = booleanPreferencesKey("camera_facing_front")
        val CAMERA_LENS_FACING = intPreferencesKey("camera_lens_facing")
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
        val STANDBY_IMAGE_URI = stringPreferencesKey("standby_image_uri")
        val PRINTED_CORNER_RADIUS = floatPreferencesKey("printed_corner_radius")
        val HEADER_ICON_SIZE = floatPreferencesKey("header_icon_size")
        val CONTINUOUS_COUNTDOWN = booleanPreferencesKey("continuous_countdown")
        val SQUARE_MODE = booleanPreferencesKey("square_mode")
        val BORDER_DESIGN_ID = intPreferencesKey("border_design_id")
        val RING_LIGHT_ENABLED = booleanPreferencesKey("ring_light_enabled")
        val SAVE_TO_DEVICE = booleanPreferencesKey("save_to_device")
        val THEME_NAME = stringPreferencesKey("theme_name")
        val START_BUTTON_OFFSET_X = floatPreferencesKey("start_button_offset_x")
        val START_BUTTON_OFFSET_Y = floatPreferencesKey("start_button_offset_y")
        val START_BUTTON_SCALE = floatPreferencesKey("start_button_scale")
        val CUSTOM_LAYOUT_TEMPLATE = stringPreferencesKey("custom_layout_template")
        val IS_ACTIVATED = booleanPreferencesKey("is_activated")
        val ACTIVATION_KEY = stringPreferencesKey("activation_key")
        
        // Printer Connection Settings
        val PRINTER_CONNECTION_TYPE = stringPreferencesKey("printer_connection_type")
        val NETWORK_PRINTER_IP = stringPreferencesKey("network_printer_ip")
        val NETWORK_PRINTER_PORT = intPreferencesKey("network_printer_port")
    }
}

