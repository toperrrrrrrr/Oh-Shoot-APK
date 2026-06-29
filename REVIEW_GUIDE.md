# Resibooth Project Review Guide

This document lists the main sections, topics, and source files to review together as we build and debug the Resibooth (formerly OhShoot) Android app.

---

## 1. 📸 Taking Photos & Capture Flow
This section covers how the app controls the device cameras, manages the live preview, plays sound effects, and handles the photo-taking countdown sequence.
*   **Key Files:**
    *   [CameraManager.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/camera/CameraManager.kt) — CameraX integration, binding preview and photo capture.
    *   [UvcCameraView.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/camera/UvcCameraView.kt) — Support hook for external UVC cameras.
    *   [CaptureSoundPlayer.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/audio/CaptureSoundPlayer.kt) — Handles countdown beeps and camera shutter sounds.
    *   [CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/ui/screens/CaptureScreen.kt) — Composable screen handling countdown, flash toggle, and UVC fallback trigger.
*   **Items to Review:** Camera resource management (releasing CameraX on pause), countdown duration settings, ring-light overlays, and front/rear camera toggle state behavior.

---

## 2. 🎨 Grid Layout & Image Processing
This section covers how captured bitmaps are resized, cropped, bordered, and compiled into print-ready collages.
*   **Key Files:**
    *   [BitmapProcessor.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/BitmapProcessor.kt) — The core engine for composing grids (`GRID_2X2`, `GRID_2X3`, `HYBRID`, strip layouts, etc.).
    *   [ImageUtils.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/util/ImageUtils.kt) — Core utilities for cropping, applying borders, and bitmap decoding.
    *   [CustomTemplate.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/domain/CustomTemplate.kt) — Configuration models for importing custom coordinate overlays.
    *   [LayoutSelectScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/ui/screens/LayoutSelectScreen.kt) & [LayoutDesignerScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/ui/screens/LayoutDesignerScreen.kt) — Screens allowing layout selection and custom positioning.
*   **Items to Review:** Grid sizing optimization, custom border/overlay calculations, performance when combining high-resolution bitmaps, and contrast boost multiplier adjustments.

---

## 3. 🖨️ Printing Pipeline & ESC/POS Translation
This section covers the translation of final layouts to a 1-bit raster graphics format using Floyd-Steinberg dithering and transmitting it via Bluetooth, USB, or Network socket.
*   **Key Files:**
    *   [EscPosBuilder.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/printer/EscPosBuilder.kt) — Low-level byte builder for formatting receipt text, alignments, paper cut actions, and rendering dithered raster bitmaps.
    *   [BluetoothPrinter.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/BluetoothPrinter.kt) — Controls the RFCOMM Bluetooth socket connection to thermal printers.
    *   [UsbPrinterManager.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/printer/UsbPrinterManager.kt) — Manages connections and raw print byte transfers to USB-plugged thermal printers.
    *   [NetworkPrinterManager.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/printer/NetworkPrinterManager.kt) — Connects and sends ESC/POS data to network printers over Port 9100.
*   **Items to Review:** ESC/POS raster generation, flow control/print speed limits (combating buffer overflows), printer error status handling (out of paper, printer disconnected), and socket cleanup.

---

## 4. ⚙️ App Settings & Theme Customization
This section covers settings storage (using Android Jetpack DataStore), the settings drawer UI, and event branding configuration.
*   **Key Files:**
    *   [AppSettings.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/domain/AppSettings.kt) — Preferences schema containing contrast boost, auto-cut settings, default camera facing, printer IP/MAC addresses, and layouts.
    *   [SettingsRepository.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/data/SettingsRepository.kt) — Repository layer for saving and retrieving operator/app configs.
    *   [SettingsPanel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/ui/components/SettingsPanel.kt) — The settings bottom-sheet panel layout.
*   **Items to Review:** Moving settings to support multi-operator configurations (operator scoping), password-protecting settings at events, and real-time settings synchronization.

---

## 5. 🏗️ App Shell & Session Management
This section covers the overarching application lifecycle, user flow from standby to print completion, and session security (blocking accidental guest navigation).
*   **Key Files:**
    *   [MainActivity.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/MainActivity.kt) — Hosts the app layout, handles overlay triggers, and initialization.
    *   [AppNavGraph.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/navigation/AppNavGraph.kt) & [Screen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/navigation/Screen.kt) — Routing and navigation logic.
    *   [SessionViewModel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/viewmodel/SessionViewModel.kt) — Manages the state machine of the active photo session (captured image buffer, active printer state, workflow transitions).
*   **Items to Review:** Enhancing the initial launch sequence check (e.g., verifying camera permissions and auto-connecting to the last known printer on the Splash Screen), session back-gesture blocking, and cleanup/reset upon session expiration.

---

## 6. 🐛 Known Bugs & Performance Risks
A list of diagnostics already compiled that we need to actively address to prevent crashes in production.
*   **Key Files:**
    *   [bugs.txt](file:///c:/Users/Nori/Desktop/OhShoot/bugs.txt) — Outline of major issues.
*   **Specific Items to Review:**
    1.  **OutOfMemoryError Risk:** Reviewing the `calculateInSampleSize` function in [ImageUtils.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/util/ImageUtils.kt) to prevent huge memory spikes with unbalanced image aspect ratios.
    2.  **TCP Socket Race Conditions:** Safeguarding concurrent connection attempts in [NetworkPrinterManager.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/printer/NetworkPrinterManager.kt) to avoid dropped print jobs.
    3.  **Bluetooth Security Exception:** Safeguarding `cancelDiscovery()` in [BluetoothPrinter.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/BluetoothPrinter.kt) on Android 12+.
    4.  **Recomposition Side Effects:** Fixing state changes fired directly within the Composable rendering path in [CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/ohshootstudio/resibooth/ui/screens/CaptureScreen.kt).
