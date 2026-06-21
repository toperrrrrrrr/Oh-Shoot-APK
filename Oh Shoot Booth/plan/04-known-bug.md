# 4. Known bug (parked, not blocking rebuild)

**Symptom:** when printing multiple pages in sequence, output sometimes degrades into garbled/gibberish characters. Recovery currently requires power-cycling the printer.

**Likely root causes** (to investigate once rebuild architecture is in place):
- Bluetooth socket/`OutputStream` not properly closed or flushed between print jobs
- ESC/POS data sent faster than the printer's internal buffer drains (no flow control / backpressure)
- Stale `OutputStream` reused after the printer's internal state resets between jobs

**Decision:** rebuild architecture first, debug after. When this is picked up, it should be diagnosed inside the new `PrinterRepository` rather than patched in the old `BluetoothPrinter.kt`. The error-code taxonomy in §6 already includes a state for this so the UI has a defined recovery path even before the underlying cause is fixed.

---

