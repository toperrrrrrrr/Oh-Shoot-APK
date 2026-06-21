package com.oh.shoot.domain

data class AppSettings(
    val cameraFacingFront: Boolean = true,
    val mirrorPreview: Boolean = true,
    val contrastBoost: Float = 1.4f,
    val paperWidth80mm: Boolean = true,
    val autoCut: Boolean = true,
    val defaultCopies: Int = 1,
    val headerText: String = "OH Shoot!",
    val footerText: String = "ohshoot.ph",
    val useBluetoothPrinter: Boolean = true,
    val businessMode: String = "Rental",
    val soundsEnabled: Boolean = true,
    val customLogoUri: String? = null,
    val printedCornerRadius: Float = 8f,
    val headerIconSizeDp: Float = 48f,
    val standbyImageUri: String? = null,
    val continuousCountdown: Boolean = false,
    val squareMode: Boolean = false,
    val borderDesignId: Int = 0,
    val ringLightEnabled: Boolean = true,
    val saveToDevice: Boolean = true,
    val themeName: String = "Dark",
    val startButtonOffsetX: Float = 0f,
    val startButtonOffsetY: Float = 0f,
    val startButtonScale: Float = 1f,
    val customLayoutTemplate: String = ""
)
