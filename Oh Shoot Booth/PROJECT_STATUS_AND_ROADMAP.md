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
| **Capture Flow** | Countdown with sound, capture sound, front/rear camera toggle. | 🟡 **Partially Implemented**<br>[CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) implements a 3-second visual countdown and plays ticks/shutter click sounds via `SoundPool` in [CaptureSoundPlayer.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/audio/CaptureSoundPlayer.kt). Camera selection is passed as state, but **there is no camera toggle button** in the Capture Screen UI itself. | Add a front/rear camera toggle button to the camera preview overlay. |
| **Fixed Layout Set** | 1-photo, 2-photo strip, 3-photo strip, 2x2 grid. | 🟡 **Partially Implemented**<br>Code in [LayoutSelectScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/LayoutSelectScreen.kt) supports `LayoutType.GRID_2X2`, `LayoutType.GRID_2X3`, and `LayoutType.HYBRID`. It is missing the 1-photo, 2-photo strip, and 3-photo strip options. | Define new layouts in `LayoutType` and implement layout rendering in [BitmapProcessor.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/BitmapProcessor.kt). |
| **Print Pipeline (§6)** | Bluetooth ESC/POS, automatic connection and retries, error codes. | 🟡 **Partially Implemented**<br>[SessionViewModel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/viewmodel/SessionViewModel.kt) handles Bluetooth printing using [BluetoothPrinter.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/BluetoothPrinter.kt), generating ESC/POS raster data with Floyd-Steinberg dithering. However, it lacks automatic connection retry logic and the error code taxonomy (§6) is not active. | Implement the printer error-status decoding and reconnection retry logic. |
| **In-App Session Lock** | Block hardware back button/gesture navigation mid-session. | 🟢 **Fully Implemented**<br>`BackHandler` is declared in [CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) and [PreviewScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/PreviewScreen.kt) to intercept back presses. | Ready. |
| **Settings Panel Scaffold**| Pull-up panel or sidebar, hidden corner trigger. | 🟢 **Implemented**<br>Implemented as a bottom sheet in [SettingsPanel.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/components/SettingsPanel.kt) accessed via a status indicator + gear icon overlay in [MainActivity.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/MainActivity.kt). | Consider adding a passcode lock to protect settings from guests at events. |
| **Auto-Cut Paper Toggle** | Option to feed and cut automatically (default: enabled). | 🟢 **Fully Implemented**<br>`AppSettings.autoCut` is stored in DataStore settings and toggled in the settings panel. The ESC/POS builder issues `feedAndCut` or `newline(6)` based on the configuration. | Ready. |
| **Ring Light Toggle** | Toggle screen-based soft ring light border. | 🔴 **Not Implemented**<br>[CaptureScreen.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/screens/CaptureScreen.kt) has a hardcoded white border acting as a software ring light, but there is no toggle in `AppSettings` or `SettingsPanel` to disable it. | Add a ring-light toggle to settings and apply the border conditionally. |

---

### Phase 2 — Customization Layer

*   **Theme & Background Customization**: 🔴 *Not Implemented*. The canvas and app background colors are hardcoded to dark colors in [Color.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/theme/Color.kt) and [Theme.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/ui/theme/Theme.kt).
*   **Drag-and-Drop Start Button**: 🔴 *Not Implemented*. Pulse circle start button is centered and fixed in size.
*   **Logo Upload (Standby & Print)**: 🟡 *Partially Implemented*. Local image logo uploading is implemented in settings and printed at the top of receipts using [ImageUtils.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/util/ImageUtils.kt) and dithered rasterization, but the logo is not yet integrated into the standby/start screen.
*   **Header/Footer Text**: 🟢 *Fully Implemented*. Custom header/footer text are persisted in [SettingsRepository.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/data/SettingsRepository.kt) and formatted on print jobs using [EscPosBuilder.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/printer/EscPosBuilder.kt).
*   **Contrast Boost Control**: 🟢 *Fully Implemented*. Persisted slider in settings adjustments contrast multipliers dynamically, which are fed into `BitmapProcessor.convertToEscPosRasterDithered`.
*   **Frame Styles**: 🔴 *Not Implemented*. `borderDesignId` is defined, and basic color borders exist in the bitmap processor, but no frame style picker UI is provided.
*   **Save-to-Device Toggle & Print Preview**: 🟡 *Partially Implemented*. Photos are always automatically saved to the gallery in `SessionViewModel`. There is no setting to toggle this behavior, and the preview screen shows a basic thumbnail list rather than how it looks inside a decorative frame.

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
*   **Symptom**: Printing multiple sheets in sequence sometimes causes corrupted or garbled text characters, requiring a printer reboot.
*   **Status**: Unresolved (parked during rebuild).
*   **Analysis**: This is typically caused by filling the printer's RX buffer faster than it can process raster commands (lack of hardware flow control over RFCOMM Bluetooth).
*   **Fix Strategy**: Update [BluetoothPrinter.kt](file:///c:/Users/Nori/Desktop/OhShoot/app/src/main/java/com/oh/shoot/BluetoothPrinter.kt) to chunk the byte streams or check print-job states between copies rather than blasting raw byte blocks.

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
