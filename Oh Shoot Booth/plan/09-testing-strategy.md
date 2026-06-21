# 9. Testing strategy

- Unit tests for `BitmapProcessor` (resize/crop/grid composition dimension checks) — JVM, no device needed
- Unit tests for `SettingsRepository` and `PrinterRepository` logic with fakes/mocks
- Instrumented tests for capture → preview → print UI flow
- Manual test checklist for real-event conditions: printer reconnect after Bluetooth drop, multi-print-in-sequence (regression check once §4 bug is fixed), session lock behavior on gesture-nav devices

---

