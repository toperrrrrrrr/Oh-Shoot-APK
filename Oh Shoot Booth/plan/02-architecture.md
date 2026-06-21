# 2. Architecture

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

