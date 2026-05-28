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
    val customLogoUri: String? = null
)
