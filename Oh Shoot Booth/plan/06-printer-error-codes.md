# 6. Printer error-code taxonomy

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

