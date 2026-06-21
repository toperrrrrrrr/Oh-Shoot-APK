# 11. Future plans & expansions

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

