# Screenshot Requirements

Screenshots must be captured on real devices or emulators at the required resolutions.

**Status: done.** Real screenshots (not mockups) exist for both platforms in
`images/phoneScreenshots/` (Android, 1080x2400), `screenshots/` (iOS 6.7", 1290x2796),
and `ipadProScreenshots/` (iPad Pro 12.9", 2048x2732), captured on a Pixel 9 Pro
emulator, an iPhone 15 Pro Max simulator, and an iPad Pro 12.9" simulator respectively,
using the sample data below.

**Note on screen #4:** the original plan called for a "Parse Review" screenshot
(AI parsing result with confidence score). That screen is no longer part of the
default capture flow — the Phase 1 instant-capture redesign made classification
happen instantly in the background with no mandatory review step, so ParseReview
is now unreachable from normal navigation. Screen #4 was replaced with the
**Calendar** view instead, which is a real, current, user-facing feature.

## Android (Google Play)

### Required Sizes
- **Phone**: 1080x1920 (16:9) or 1080x2400 (20:9) — min 2, max 8
- **7" Tablet**: 1200x1920 — optional but recommended
- **10" Tablet**: 1600x2560 — optional but recommended

### Screenshots (in order)
1. **Today View** — Task list with a few sample tasks showing priorities and due dates
2. **Quick Capture** — Text input screen with example text being entered
3. **Voice Capture** — Voice recording screen (Android: idle "Tap to speak" state,
   since the emulator's on-device speech recognizer doesn't function; iOS: real
   "Listening..." state, since the simulator's speech recognition works)
4. **Calendar** — Month view with color-coded priority dots (replaces Parse Review, see above)
5. **Settings** — Settings screen, free plan
6. **Paywall** — Premium upgrade bottom sheet with feature list
7. **Notes** — Note list view with sample notes
8. **Upcoming Tasks** — Upcoming view with tasks grouped by date
9. **Settings / Premium** — Settings screen showing premium status section (Android:
   real "All features unlocked" state, set via a local debug override; iOS: same
   free-plan screenshot as #5 reused — StoreKit sandbox reconciliation resets a
   manually-set premium flag on relaunch, so a clean premium shot needs either a
   real sandbox purchase or a temporary code patch, neither done here)

### Feature Graphic
- **Size**: 1024x500 PNG
- **Content**: App icon + tagline "Capture ideas instantly. AI structures them into tasks."

## iOS (App Store)

### Required Sizes
- **6.7" (iPhone 15 Pro Max)**: 1290x2796 — required, **done** (in `screenshots/`)
- **6.5" (iPhone 14 Plus)**: 1284x2778 — required unless App Store Connect auto-scales from 6.7", **not yet captured**
- **5.5" (iPhone 8 Plus)**: 1242x2208 — required if supporting older devices, **not yet captured**
- **iPad Pro 12.9"**: 2048x2732 — required if iPad supported, **done** (in `ipadProScreenshots/`)

Verify at upload time in App Store Connect whether the 6.7" set is sufficient for
phone sizes or whether additional sizes are still required — this changes
periodically as Apple's device lineup changes.

### Screenshots (in order)
Same as Android list above.

## Capture Instructions

1. Set device to light mode, Wi-Fi on, full battery, time set to 9:41 (iOS) or clean status bar
2. Populate the app with sample data:
   - "Call dentist tomorrow at 2pm" (task with date + time)
   - "Buy groceries #errands" (task with tag)
   - "Great pasta recipe at the Italian place" (note)
   - "Submit quarterly report by Friday high priority" (high priority task)
   - "Interesting idea about neural networks and creativity" (note)
3. Capture each screen in the order listed above
4. Place files named `01_today.png`, `02_text_capture.png`, `03_voice_capture.png`,
   `04_calendar.png`, `05_settings.png`, `06_paywall.png`, `07_notes.png`,
   `08_upcoming.png`, `09_settings_premium.png` in:
   - Android phone: `images/phoneScreenshots/`
   - iOS 6.7": `screenshots/`
   - iOS iPad Pro 12.9": `ipadProScreenshots/`
