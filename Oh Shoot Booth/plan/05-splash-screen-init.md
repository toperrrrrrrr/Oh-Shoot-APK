# 5. Splash screen — initialization checklist

The splash screen is responsible for everything that needs to be ready before standby loads:

- [ ] Bluetooth adapter state + permission check (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`)
- [ ] Attempt printer reconnect (last known paired device)
- [ ] Camera permission check
- [ ] Load active operator's settings/theme config from local storage
- [ ] Warm asset cache for the active event (logo, background, sounds) — avoid first-load jank on standby
- [ ] Apply in-app session lock setup
- [ ] Check for previous-session recovery state — if the app was killed mid-session, decide: resume to standby cleanly, or surface a brief error/reset state. **Needs a decision before phase 1 is complete; default to silent reset to standby unless data suggests otherwise.**

---

