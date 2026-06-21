# Project Status & Rebuild Roadmap — Resibooth

This document provides a comprehensive review of the current implementation of the Resibooth (formerly OhShoot) Android codebase against the original project plan, highlighting completed work, architectural deviations, and a step-by-step roadmap for continuing development.

---

## 📊 Summary of Implementation Progress

Below is the current state of progress across the planned features:

| Phase | Total Features | Completed | Partially Implemented | Not Implemented | Implementation Rate |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Phase 1: Core Loop** | 9 | 4 | 4 | 1 | ~65% |
| **Phase 2: Customization** | 7 | 1 | 2 | 4 | ~20% |
| **Phase 3: Expansion** | 6 | 1 | 0 | 5 | ~15% |

---

## 🔍 Detailed Component Audit (Planned vs. Actual)

### Phase 1 — Core Loop

| Feature | Planned Requirement | Current Code Implementation Status | Action Required |
| :--- | :--- | :--- | :--- |
| **Splash/Init Sequence (§5)** | Check Bluetooth adapter + permissions, attempt printer reconnect, check camera permission, load operator config, warm asset cache, apply session lock. | 🟡 **Partially Implemented**<br>A simple `SplashScreen` exists in [SplashAndStandby.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/SplashAndStandby.kt) but only performs a hardcoded 2-second delay. Camera permission check is deferred until the capture screen. | Move permission checks and printer reconnect logic to the splash sequence. |
| **Standby Screen** | Pulsing-circle start button with event branding. | 🟢 **Fully Implemented**<br>`StandbyScreen` in [SplashAndStandby.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/SplashAndStandby.kt) uses Jetpack Compose infinite transitions to scale a gold pulsing circle with custom typography. | Ready. |
| **Capture Flow** | Countdown with sound, capture sound, front/rear camera toggle. | 🟢 **Fully Implemented**<br>[CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) implements a 3-second visual countdown, plays sounds via `SoundPool`, and has a front/rear camera toggle button in the bottom overlay. | Ready. |
| **Fixed Layout Set** | 1-photo, 2-photo strip, 3-photo strip, 2x2 grid. | 🟢 **Fully Implemented**<br>Code in [LayoutSelectScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/LayoutSelectScreen.kt) and [BitmapProcessor.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/BitmapProcessor.kt) supports all standard layout types (SINGLE, STRIP_2, STRIP_3, GRID_2X2, GRID_2X3, HYBRID). Thumbnail columns are optimized in [PreviewScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/PreviewScreen.kt). | Ready. |
| **Print Pipeline (§6)** | Bluetooth ESC/POS, automatic connection and retries, error codes. | 🟢 **Fully Implemented**<br>[SessionViewModel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/viewmodel/SessionViewModel.kt) handles Bluetooth/USB printing. The `OutputScreen` has been updated with a manual reconnect/retry button to re-run printer diagnostics. | Ready. |
| **In-App Session Lock** | Block hardware back button/gesture navigation mid-session. | 🟢 **Fully Implemented**<br>`BackHandler` is declared in [CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) and [PreviewScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/PreviewScreen.kt) to intercept back presses. | Ready. |
| **Settings Panel Scaffold**| Pull-up panel or sidebar, hidden corner trigger. | 🟢 **Implemented**<br>Implemented as a bottom sheet in [SettingsPanel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/components/SettingsPanel.kt) accessed via a status indicator + gear icon overlay in [MainActivity.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/MainActivity.kt). | Consider adding a passcode lock to protect settings from guests at events. |
| **Auto-Cut Paper Toggle** | Option to feed and cut automatically (default: enabled). | 🟢 **Fully Implemented**<br>`AppSettings.autoCut` is stored in DataStore settings and toggled in the settings panel. The ESC/POS builder issues `feedAndCut` or `newline(6)` based on the configuration. | Ready. |
| **Ring Light Toggle** | Toggle screen-based soft ring light border. | 🟢 **Fully Implemented**<br>Integrated into `AppSettings` and toggle available in settings panel. | Ready. |

---

### Phase 2 — Customization Layer

*   **Theme & Background Customization**: 🟢 **Fully Implemented**. Added four theme presets (Dark, Light, Pink, Midnight Blue) stored in `AppSettings` and applied dynamically via `Theme.kt`.
*   **Drag-and-Drop Start Button**: 🟢 **Fully Implemented**. Added "Edit Standby Layout" setting which enables moving and scaling the start button on the Standby Screen using Compose gestures.
*   **Logo Upload (Standby & Print)**: 🟢 **Fully Implemented**. Integrated into prints via rasterization and dynamically rendered on the `StandbyScreen` via Coil `AsyncImage`.
*   **Header/Footer Text**: 🟢 *Fully Implemented*. Custom header/footer text are persisted in [SettingsRepository.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/data/SettingsRepository.kt) and formatted on print jobs using [EscPosBuilder.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/printer/EscPosBuilder.kt).
*   **Contrast Boost Control**: 🟢 *Fully Implemented*. Persisted slider in settings adjustments contrast multipliers dynamically, which are fed into `BitmapProcessor.convertToEscPosRasterDithered`.
*   **Frame Styles**: 🟢 **Fully Implemented**. Added UI picker in `SettingsPanel` to select "None", "Thin Black", or "Polaroid".
*   **Save-to-Device Toggle & Print Preview**: 🟢 **Fully Implemented**. Added `saveToDevice` toggle in settings. The preview screen now displays an exact preview of the layout using the actual print framing output, with a thumbnail strip for retakes.

---

### Phase 3 — Advanced Layout + Hardware Expansion

*   **Hybrid Drag-and-Drop Layout Designer**: 🔴 *Not Implemented*.
*   **Wired Printer Support**: 🔴 *Not Implemented*.
*   **USB Printer Support**: 🟢 **Implemented early!** A raw [UsbPrinterManager.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/printer/UsbPrinterManager.kt) class is fully implemented and used to send ESC/POS byte buffers to USB thermal printers.
*   **External Camera Support**: 🔴 *Not Implemented*.
*   **DSLR-booth Template Import**: 🔴 *Not Implemented*.

---

## 📐 Architectural Deviations from Original Plan

### 1. Single Module vs. Multi-Module
*   **Target Plan**: Split codebase into Gradle modules: `:app` (shell), `:core:ui`, `:core:data`, and features (`:feature:standby`, `:feature:capture`, `:feature:preview`, `:feature:printing`, `:feature:settings`).
*   **Current State**: Everything resides inside a single `:app` module. The internal packages are clean, but physical module boundaries are not set up.
*   **Recommendation**: Restructuring should be executed in early Phase 1.5 before code complexity increases.

### 2. Missing Operator Scoping
*   **Target Plan**: Every repository method touching configurations must accept an `operatorId` parameter to prepare for future multi-operator B2B support.
*   **Current State**: Repositories are flat, and settings are saved globally.
*   **Recommendation**: Refactor `AppSettings` and `SettingsRepository` to index preferences by operator keys.

---

## 🐛 Known Bugs & Diagnostic Status

### Multiple Print Sequence Gibberish Output
*   Symptom: Printing multiple sheets in sequence sometimes causes corrupted or garbled text characters, requiring a printer reboot.
*   Status: 🟢 **Resolved**.
*   Analysis: Rapid RFCOMM socket connect/disconnect commands overwhelmed the printer's asynchronous RX queue.
*   Fix: Modified [BluetoothPrinter.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/BluetoothPrinter.kt) to accept a copies count parameter, holding the socket open across multiple print jobs and inserting a 1-second delay between copies to let hardware buffers settle.

---

## 🚀 Step-by-Step Continuation Roadmap

Here is the proposed sequence of tasks to continue developing the project from this state:

### Step 1: Modularize the Gradle Project (Infrastructure)
1. Split the root Gradle project into sub-modules:
   - `:core:ui` (Theme system, custom typography, standard Dialogs/Buttons).
   - `:core:data` (Repository interfaces for Settings and Printers + local SQLite/DataStore data sources).
   - `:feature:standby` (Standby screen).
   - `:feature:capture` (Camera preview, countdown, and CaptureScreen).
   - `:feature:preview` (Layout selection, collage preview, copy stepper).
   - `:feature:settings` (Settings sheet).
   - `:feature:printing` (ESC/POS builder, Bluetooth printer, USB printer).
2. Update [settings.gradle.kts](file:///c:/Users/Nori/Desktop/OhShoot/settings.gradle.kts) and adjust Hilt dependency injections.

### Step 2: Implement Operator-Scoped Repository APIs
1. Update `SettingsRepository` functions to accept `operatorId: String` parameters.
2. Refactor DataStore storage to save settings under operator-scoped preferences.

### Step 3: Upgrade Splash and Standby Initialization Flow
1. Move the camera and Bluetooth permission check launchers from [CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) to the [SplashScreen](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/SplashAndStandby.kt) initialization sequence.
2. Automatically prompt for reconnection to the last connected printer before displaying the "TAP TO START" standby interface.

### Step 4: Implement Missing Layouts and Capture Screen Features
1. Add Layout types for **Single Photo**, **2-Photo Strip**, and **3-Photo Strip**.
2. Update rendering logic in `combineBitmapsToGrid` to format these layouts into vertical thermal print strips.
3. Add a front/rear camera toggle action to the capture screen UI overlay.
4. Add the Ring Light toggle switch in settings to hide/show the thick white preview border.

### Step 5: Implement Error Status Mapping & Reconnect Pipeline
1. Bind raw ESC/POS status command checks (e.g. `DLE EOT n` or reading printer returns) in `BluetoothPrinter` or `UsbPrinterManager` to populate the `PrinterState` with specific errors (`PRINTER_OUT_OF_PAPER`, `PRINTER_JAM`, etc.).
2. Show actionable warnings on the `OutputScreen` with explicit "Retry" and "Reconnect" triggers.
