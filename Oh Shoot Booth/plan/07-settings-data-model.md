# 7. Settings data model (outline)

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

