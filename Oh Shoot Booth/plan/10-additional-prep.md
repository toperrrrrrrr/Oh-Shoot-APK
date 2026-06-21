# 10. Additional prep — beyond the build itself

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

