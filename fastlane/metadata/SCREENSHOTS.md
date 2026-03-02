# Screenshot Requirements

Screenshots must be captured on real devices or emulators at the required resolutions.

## Android (Google Play)

### Required Sizes
- **Phone**: 1080x1920 (16:9) or 1080x2400 (20:9) — min 2, max 8
- **7" Tablet**: 1200x1920 — optional but recommended
- **10" Tablet**: 1600x2560 — optional but recommended

### Recommended Screenshots (in order)
1. **Today View** — Task list with a few sample tasks showing priorities and due dates
2. **Quick Capture** — Text input screen with example text being entered
3. **Voice Capture** — Voice recording screen with waveform visualization
4. **Parse Review** — AI parsing result showing extracted task with confidence score
5. **Settings / Premium** — Settings screen showing premium status section
6. **Paywall** — Premium upgrade bottom sheet with feature list
7. **Notes** — Note list view with sample notes
8. **Upcoming Tasks** — Upcoming view with tasks grouped by date

### Feature Graphic
- **Size**: 1024x500 PNG
- **Content**: App icon + tagline "Capture ideas instantly. AI structures them into tasks."

## iOS (App Store)

### Required Sizes
- **6.7" (iPhone 15 Pro Max)**: 1290x2796 — required
- **6.5" (iPhone 14 Plus)**: 1284x2778 — required
- **5.5" (iPhone 8 Plus)**: 1242x2208 — required if supporting older devices
- **iPad Pro 12.9"**: 2048x2732 — required if iPad supported

### Recommended Screenshots (in order)
Same as Android list above. Apple allows up to 10 per device size.

## Capture Instructions

1. Set device to light mode, Wi-Fi on, full battery, time set to 9:41 (iOS) or clean status bar
2. Populate the app with sample data:
   - "Call dentist tomorrow at 2pm" (task with date + time)
   - "Buy groceries #errands" (task with tag)
   - "Great pasta recipe at the Italian place" (note)
   - "Submit quarterly report by Friday high priority" (high priority task)
   - "Interesting idea about neural networks and creativity" (note)
3. Capture each screen in the order listed above
4. Place files in the corresponding `images/phoneScreenshots/` directory named `01_today.png`, `02_capture.png`, etc.
