# Resibooth — Project Plan

Android photobooth kiosk app. Tablet-based, thermal-printer output, built for live events. This document is the source of truth for the rebuild — supersedes the original OhShoot POC notes.

Status: planning / pre-rebuild
Owner: internal use first, multi-operator support planned
Target hardware: Xiaomi Redmi Pad Pro 5G (XP-N160II printer)

---

## 1. Product summary

Resibooth turns a tablet into a self-serve event photobooth. A guest taps to start, the app runs a countdown and capture sequence, composes the shot(s) into a layout, and prints instantly via a connected thermal (or future wired/USB) printer. No operator required during a session.

Positioned as a software-first alternative to DSLR photobooth rental rigs.

**Primary user (now):** the app owner, running their own photobooth business.
**Primary user (later):** other photobooth operators, each with their own branding/printer/settings — the app must be operator-scoped internally even before this ships.

---

## 2. Architecture

**Pattern:** MVVM + Repository. Not full Clean Architecture — the complexity here is concentrated in two naturally-isolated areas (image processing, printer I/O), which don't need an additional use-case layer on top of repositories to stay maintainable.

**Why this over Clean Architecture:** Clean Architecture's value is in decoupling business rules from frameworks across large teams or deeply layered domains. Resibooth's domain logic is thin — repositories already provide enough abstraction to swap local storage for cloud sync later without touching ViewModels or UI.

### Module structure

```
:app                    — application shell, navigation, kiosk-mode lock
:core:ui                — shared Compose components, theme system
:core:data              — repository interfaces + local implementations
:feature:standby        — tap-to-start screen
:feature:capture        — camera, countdown, capture session
:feature:preview        — layout composition, print preview
:feature:printing       — printer connection, ESC/POS, error handling
:feature:settings       — settings panel (printer, camera, theme, layout)
```

### Layers (top to bottom)

1. **UI (Compose)** — screens observe ViewModel state, no business logic.
2. **ViewModel** — `SessionViewModel`, `PrintViewModel`, `SettingsViewModel`. Holds UI state, survives rotation/process death.
3. **Repository interfaces** (`:core:data`) — `SettingsRepository`, `PrinterRepository`, `BitmapProcessor`. ViewModels depend on interfaces, never concrete implementations.
4. **Data sources** — local now (Room + DataStore for settings, Bluetooth socket for printer, in-memory/coroutines for image processing). Cloud implementations added later behind the same interfaces.

**Operator scoping rule:** every repository method that touches settings, themes, or printer config takes an `operatorId` parameter, even though only one hardcoded operator exists today. This is the one piece of foresight that prevents a painful migration when multi-operator support ships. Example:

```kotlin
interface SettingsRepository {
    suspend fun getTheme(operatorId: String): ThemeConfig
    suspend fun saveTheme(operatorId: String, theme: ThemeConfig)
    suspend fun getPrinterConfig(operatorId: String): PrinterConfig
}
```

### Dependency injection

Use Hilt. Wire repositories as singletons scoped to the app, ViewModels scoped to their navigation graph.

### Kiosk / session lock

Simple in-app lock, not full device-owner kiosk mode:
- Intercept back button during an active session (`BackHandler` no-ops or redirects to standby).
- `FLAG_KEEP_SCREEN_ON` during sessions.
- Disable status bar expansion where possible.

**Known limitation:** this does not block the recents button or gesture-nav home swipe. If guests are observed accidentally backgrounding the app at real events, that's the trigger to revisit full device-owner kiosk mode — not a sign the architecture needs to change.

### Local-first, cloud-ready

All settings/theme/operator data lives locally (Room + DataStore) for now. Repository interfaces are written so a future `CloudSettingsRepository` can be swapped in without touching ViewModels or UI. No cloud work happens until multi-operator support is actually being built.

---

## 3. Build phases

### Phase 1 — Core loop (must work flawlessly before anything else)

- [ ] Splash/init sequence (see §5)
- [ ] Standby screen — default pulsing-circle brand style, tap to start
- [ ] Capture flow — countdown with sound, capture sound, front/rear camera toggle
- [ ] Fixed layout set: 1-photo, 2-photo strip, 3-photo strip, 2x2 grid
- [ ] Print pipeline — Bluetooth ESC/POS, connect/retry, error codes (see §6)
- [ ] In-app session lock (no back navigation mid-session)
- [ ] Settings panel scaffold — pull-up panel or sidebar, hidden corner trigger (placeholders OK for phase 2/3 items)
- [ ] Auto-cut paper toggle (default: enabled)
- [ ] Ring light toggle

### Phase 2 — Customization layer

- [ ] Theme/background customization per event
- [ ] Drag-and-drop placement of start button (shape, position)
- [ ] Logo upload (standby screen + printed output)
- [ ] Header/footer text and image modification
- [ ] Contrast boost control
- [ ] Frame styles for printed/saved output
- [ ] Save-to-device toggle, with print-appearance preview (how it looks framed)

### Phase 3 — Advanced layout + hardware expansion

- [ ] Hybrid drag-and-drop layout designer (user places image sections freely on the paper)
- [ ] Wired printer support — distinct integration from Bluetooth, define after phase 1 ships
- [ ] USB printer support (e.g. Canon Pixma class devices) — **not an ESC/POS toggle.** Standard inkjet/photo printers need Android's `PrintManager`/Mopria framework, which is a separate pipeline from raster-over-Bluetooth. Scope as its own feature, not an extension of `PrinterRepository`'s Bluetooth path.
- [ ] External camera support (webcam/DSLR via USB) — tablet camera is the v1 baseline; this is an additive capture source behind the same capture interface, not a replacement
- [ ] DSLR-booth template import (accept external template formats)
- [ ] Additional camera/printer integrations (open-ended, evaluate as requested)

### Deferred — needs definition before scoping

- [ ] QR code feature — purpose undefined (digital delivery? gallery link? social share?). Define use case before adding to data model.
- [ ] Physical enclosure/box dimensions — blocked on final tablet mount, camera placement, ring light position, and printer housing decisions.

---

## 4. Known bug (parked, not blocking rebuild)

**Symptom:** when printing multiple pages in sequence, output sometimes degrades into garbled/gibberish characters. Recovery currently requires power-cycling the printer.

**Likely root causes** (to investigate once rebuild architecture is in place):
- Bluetooth socket/`OutputStream` not properly closed or flushed between print jobs
- ESC/POS data sent faster than the printer's internal buffer drains (no flow control / backpressure)
- Stale `OutputStream` reused after the printer's internal state resets between jobs

**Decision:** rebuild architecture first, debug after. When this is picked up, it should be diagnosed inside the new `PrinterRepository` rather than patched in the old `BluetoothPrinter.kt`. The error-code taxonomy in §6 already includes a state for this so the UI has a defined recovery path even before the underlying cause is fixed.

---

## 5. Splash screen — initialization checklist

The splash screen is responsible for everything that needs to be ready before standby loads:

- [ ] Bluetooth adapter state + permission check (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`)
- [ ] Attempt printer reconnect (last known paired device)
- [ ] Camera permission check
- [ ] Load active operator's settings/theme config from local storage
- [ ] Warm asset cache for the active event (logo, background, sounds) — avoid first-load jank on standby
- [ ] Apply in-app session lock setup
- [ ] Check for previous-session recovery state — if the app was killed mid-session, decide: resume to standby cleanly, or surface a brief error/reset state. **Needs a decision before phase 1 is complete; default to silent reset to standby unless data suggests otherwise.**

---

## 6. Printer error-code taxonomy

Defined now so the error-handling UI can be built before the gibberish bug itself is root-caused.

| Code | Condition | User-facing message | Recovery action |
|------|-----------|---------------------|------------------|
| `PRINTER_NOT_PAIRED` | No printer selected/paired | "No printer connected" | Open printer selection in settings |
| `PRINTER_DISCONNECTED` | Bluetooth connection dropped mid-session or mid-print | "Printer disconnected" | Auto-retry connect, manual retry button |
| `PRINTER_OUT_OF_PAPER` | Paper-out sensor signal (if supported) or print failure pattern matching it | "Out of paper" | Manual — operator refills, retry button |
| `PRINTER_LOW_PAPER` | Paper-low sensor signal, if the printer model supports it | "Paper running low" | Non-blocking warning, no retry needed |
| `PRINTER_JAM_OR_CUTTER_FAULT` | Cutter/feed error response | "Printer jam — check paper path" | Manual intervention, retry button |
| `PRINTER_OUTPUT_CORRUPTED` | Garbled/gibberish output detected or repeated print failures in sequence (the known bug) | "Print error — reconnecting" | Force disconnect/reconnect cycle, retry. Long-term fix tracked in §4 |
| `PRINTER_BUFFER_TIMEOUT` | No ack within expected window | "Printer not responding" | Retry with backoff, fall back to manual reconnect after N attempts |

Settings → Printer should expose: connection status indicator, printer selection/refresh list, manual retry, and a disabled placeholder section for future printer-type-specific settings (the spec only defines 80mm thermal as the live option today).

---

## 7. Settings data model (outline)

Settings are scoped per-operator (`operatorId`) even with one hardcoded operator today.

```
OperatorConfig
├── printer: PrinterConfig
│   ├── type: ENUM (THERMAL_80MM, [future: WIRED, USB_PIXMA])
│   ├── pairedDeviceAddress: String?
│   ├── autoCutEnabled: Boolean (default: true)
│   └── typeSpecificSettings: Map<String, Any> (placeholder, disabled in UI until type added)
├── camera: CameraConfig
│   ├── defaultFacing: ENUM (FRONT, REAR)
│   ├── countdownSeconds: Int
│   ├── countdownSoundEnabled: Boolean
│   ├── captureSoundEnabled: Boolean
│   └── ringLightEnabled: Boolean
├── layout: LayoutConfig
│   ├── activeLayoutType: ENUM (SINGLE, STRIP_2, STRIP_3, GRID_2X2, HYBRID)
│   ├── paperLengthDefault: Float
│   └── hybridSections: List<SectionPlacement>? (phase 3)
├── theme: ThemeConfig
│   ├── standbyBackgroundUri: String?
│   ├── startButtonShape / position: enum / coordinates
│   ├── logoUri: String?
│   ├── headerText / footerText: String?
│   └── contrastBoost: Float
└── output: OutputConfig
    ├── saveToDeviceEnabled: Boolean
    └── frameStyle: ENUM
```

This is a starting outline, not final — expect fields to shift once phase 2 UI is being built against it.

---

## 8. Assets to prepare

- App logo (brand default for standby screen)
- Countdown beep audio
- Capture sound
- Default theme background(s)
- Default frame style assets

---

## 9. Testing strategy

- Unit tests for `BitmapProcessor` (resize/crop/grid composition dimension checks) — JVM, no device needed
- Unit tests for `SettingsRepository` and `PrinterRepository` logic with fakes/mocks
- Instrumented tests for capture → preview → print UI flow
- Manual test checklist for real-event conditions: printer reconnect after Bluetooth drop, multi-print-in-sequence (regression check once §4 bug is fixed), session lock behavior on gesture-nav devices

---

## 10. Additional prep — beyond the build itself

These aren't code tasks, but they determine whether the rebuilt app actually works at a real event. Worth addressing before or alongside Phase 1, not after.

### Privacy, consent, and data handling
The app captures photos of real people — often minors at family events — and may store, print, or (per §11 future plans) deliver them digitally. This needs attention now, before any data-retention behavior is hardcoded:
- [ ] Decide a default photo retention policy (auto-delete after N hours/days, or keep until manually cleared) and expose it as a setting, not a hardcoded behavior
- [ ] Decide whether any consent screen is needed before capture (varies by jurisdiction and venue — some venue contracts require this)
- [ ] If photos are ever uploaded anywhere (cloud sync, guest gallery, social share — all in §11 future plans), a privacy policy becomes a hard requirement, not optional. Draft this before any of those features ship, even if it sits unused for now
- [ ] Decide what happens to captured photos if the app crashes mid-session — are partial captures purged or retained for recovery?

### Physical operations
- [ ] Power plan: wall outlet dependency vs. battery pack, and required runtime for a typical 4–8 hour event
- [ ] Paper logistics: roll capacity, recommended spare rolls per event length, and a "low paper" warning threshold in the printer error taxonomy (§6 currently only covers out-of-paper, not low-paper-warning)
- [ ] Pre-event operator checklist (non-code, a literal printed or in-app reference): charge tablet, pair printer, run a test print, confirm paper loaded, confirm theme/branding set for the event
- [ ] On-site failure quick-reference — maps each error code in §6 to a one-line human action, so an operator without technical background can self-resolve common issues without calling you

### Release & distribution
- [ ] Decide distribution method: sideloaded APK (simplest for kiosk-locked single-purpose tablets, but no auto-update), Play Store (adds vetting/update convenience but complicates kiosk-mode behavior), or internal distribution (e.g. Firebase App Distribution) for early multi-operator rollout
- [ ] Update strategy for tablets already deployed in the field — especially relevant once this is used by other operators who won't be manually pulling new builds
- [ ] Versioning/changelog convention, so issues reported from a specific tablet can be traced to a specific build

### Legal & business (relevant once this moves toward other operators)
- [ ] Terms of service / licensing terms for operators, once monetization (§11 future plans) is pursued
- [ ] Liability considerations for venue/event use — who's responsible if hardware fails during a paid event booking

---

## 11. Future plans & expansions

Longer-horizon ideas, beyond the phase 1–3 build plan in §3. None of these are scoped or committed — listed here so they're not lost, and so early architecture decisions (operator scoping, repository interfaces, capture/print pipelines) don't accidentally foreclose them.

### Multi-operator / B2B platform
- Operator onboarding flow — account creation, branding setup, printer pairing wizard
- Per-operator billing (see monetization models below)
- Operator dashboard (likely a separate web app, not in-tablet) for managing themes/templates across multiple tablets/events without touching each device
- Fleet management — push a theme/config update to multiple tablets tied to one operator account
- Usage analytics per operator (prints per event, most-used layouts, uptime/error rates) — useful both for the operator and for product decisions

### Hardware & integrations
- Additional thermal printer brands/models beyond the current XP-N160II target — printer abstraction in `PrinterRepository` should anticipate this (see §2)
- Additional camera integrations — action cams, mirrorless via capture card, multi-camera angle selection for a session
- NFC or QR-based session handoff (tap a phone to retrieve digital copies) — depends on the QR feature decision in §3
- Alternate enclosure form factors (tabletop vs. floor-standing vs. wall-mounted) once the core box design is finalized
- Battery/power management for untethered outdoor events (no wall outlet)

### Template & theme ecosystem
- Marketplace or library of pre-built event themes (wedding, corporate, birthday, holiday) — ties into the "moat" idea of a growing theme library competitors can't easily replicate
- Support for importing third-party DSLR-booth template formats (already noted in phase 3) — expand into a defined template spec others can design against
- Seasonal/limited-time theme drops tied to holidays or trending events
- Community or operator-submitted templates, with a review/approval step

### Guest-facing features
- Digital delivery of photos — QR code, SMS, or email handoff (purpose for QR needs to be settled first, see §3)
- Social share — direct post to Instagram/etc. from the print preview screen
- Guest gallery — event-scoped web page showing all captures from a session (raises data retention/privacy questions to resolve before building)
- GIF/boomerang capture mode as an alternative to static photo layouts
- Video booth mode (short clips instead of stills) — would require a meaningfully different capture/processing pipeline, treat as a separate feature track rather than an extension of `BitmapProcessor`

### Monetization-related (ties to earlier framework discussion)
- Per-event or per-print usage billing for operators on a usage-based tier
- Tiered subscription (basic layouts vs. full customization/theme library access)
- Hardware bundle — "Resibooth in a box" kit sales (tablet + printer + enclosure), separate from the software license

### Platform/technical
- iOS version — would require re-evaluating camera, Bluetooth, and printing implementations natively rather than porting Kotlin directly
- Web-based remote configuration (manage tablet settings from a browser instead of the in-app settings panel)
- Offline-first conflict resolution once cloud sync (§2) ships and an operator manages multiple tablets that could go offline independently

---

## 12. Explicitly out of scope for this rebuild

- Full Android device-owner kiosk mode (deferred until in-app lock proves insufficient)
- Cloud sync (deferred until multi-operator work begins)
- USB/wired printer and external camera support (phase 3, separate pipelines)
- QR code feature (undefined purpose — needs a decision first)
- Physical enclosure design (blocked on hardware placement decisions)

---

*Restart plan generated 2026-06-20. Supersedes the original OhShoot POC restart guide.*
