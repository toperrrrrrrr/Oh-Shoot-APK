# 3. Build phases

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

