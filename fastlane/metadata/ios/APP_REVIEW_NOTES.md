# App Store Connect — App Review Information Notes

Paste the **“App Review Notes (paste into App Store Connect)”** section below into  
**App Store Connect → Your App → App Review Information → Notes**.

Also attach (or upload when the form allows) a **physical-device screen recording**  
following the script in §1. Apple requires that recording for this response; text alone  
is not enough for item 1.

---

## 1. Screen recording script (physical device)

**Requirements (Apple):** Physical device, latest iOS, start with app launch, show the  
typical core flow. Include: account flows (N/A), paid/purchase flows, UGC/report/block  
(N/A), and sensitive-permission prompts (microphone / speech recognition).

### Recommended recording path (~2–4 minutes)

1. **Launch** LocalMind from the home screen (cold start).
2. **Today** — show the task list (empty is fine; or create items first so the list isn’t blank).
3. **Quick Capture (Text)**  
   - Tap **+ Capture** → **Text** tab.  
   - Type something like: `Call dentist tomorrow at 2pm high priority`.  
   - Tap **Capture**.  
   - Return to **Today** / **Upcoming** and show the structured task (due date/priority if shown).
4. **Quick Capture (Voice)**  
   - Tap **+ Capture** → **Voice**.  
   - When iOS prompts for **Microphone** and/or **Speech Recognition**, **Allow** (show the system prompts on camera).  
   - Tap the mic / speak a short phrase → Capture.  
   - (If you prefer not to speak on camera: start Listening so the UI is clear, then cancel and use text only — but still show the permission prompts once.)
5. **Notes** — open **Notes**, create a short note (e.g. `Great pasta recipe at the Italian place`).
6. **Calendar** — open **Calendar**, show month view with any task dots.
7. **Upcoming** — open **Upcoming**, show date-grouped tasks.
8. **Settings + Premium paywall**  
   - Open **Settings** (gear).  
   - Show Free Plan / Upgrade.  
   - Tap **Upgrade** → show paywall with **Lifetime** and **Monthly** options.  
   - Optionally start a **sandbox** purchase (sandbox Apple ID) and complete or cancel.  
   - Tap **Restore Purchases** or **Maybe Later** as needed.
9. **Optional Premium path** (if sandbox purchase succeeds): enable on-device LLM / show Export Tasks if unlocked.

### Explicit N/As (state on camera or in notes)

- **No account registration, login, or account deletion** — there are no user accounts.
- **No user-generated content community** — notes/tasks are private and local only; no feed, sharing social graph, reporting, or blocking of other users.
- **No location, contacts, camera, photos library, or App Tracking Transparency** prompts — the app does not request these.

### How to capture on device

Settings → Control Center → Screen Recording (or QuickTime on a Mac with the phone connected).  
Export MP4 and upload/attach per App Store Connect’s current review response UI.

---

## 2–7. App Review Notes (paste into App Store Connect)

Copy everything between the lines below into the **Notes** field.

---

### APP REVIEW NOTES — LocalMind (Local Minder)

**App:** LocalMind (listed as Local Minder)  
**Bundle ID:** com.markduenas.localmind.localmind  
**Business model:** Free to download. Optional in-app purchases for Premium.

---

#### 1) Screen recording

A screen recording captured on a **physical device** running a recent iOS release is provided with this response / attached as requested. The recording starts with launching the app and demonstrates: Today list, text capture → structured task, voice capture including system permission prompts (microphone and/or speech recognition), Notes, Calendar, Upcoming, Settings, and the Premium paywall (StoreKit purchase UI).

**Not applicable (by design):**
- Account registration, login, or account deletion — the app has **no accounts**.
- User-generated content reporting/blocking — content is private and on-device only; there is no multi-user content network.
- Location, contacts, camera, photo library, or App Tracking Transparency — not used.

---

#### 2) Devices and OS versions tested before submission

Testing before this submission included:

| Device | OS |
|--------|-----|
| iPhone 15 Pro Max (Simulator) | iOS 18.x (store screenshot / QA) |
| iPad Pro 12.9" (Simulator) | iOS 18.x (store screenshot / QA) |
| Physical iPhone (developer device used for TestFlight / device builds) | Latest iOS available at test time (please keep this line accurate to your actual hardware, e.g. iPhone 15 / iOS 18.x) |

**Minimum supported:** iOS as configured in the Xcode project (iOS 18.2+ per project store docs).  
**Android (for completeness of multiplatform QA, not this iOS binary):** Android API 24+ (emulator + device as applicable).

*Update the physical device row to match the exact model and iOS version used for the attached recording.*

---

#### 3) Purpose and target audience

**Purpose:** LocalMind is a privacy-first task and note manager. Users capture ideas by **voice or text**; on-device parsing turns natural language into structured tasks (due dates, priorities, tags) and notes—without requiring a cloud account.

**Problem it solves:** People lose ideas in ephemeral voice memos or unstructured notes. Existing task apps often require accounts and send data to servers. LocalMind keeps capture fast and **local**, with optional smarter on-device AI for Premium users.

**Target audience:** Privacy-conscious professionals, students, and anyone who wants a simple offline-capable capture → task workflow without signing up.

**Value:** Instant capture, structured tasks and notes, calendar/upcoming views, optional local notifications, encrypted local storage, and optional Premium for on-device LLM parsing and JSON export.

---

#### 4) Setup and how to access main features

**No login credentials.** No sample account. No server-side setup.

**Reviewer flow:**
1. Install and launch the app (free).
2. Grant **Microphone** / **Speech Recognition** if testing voice (optional; text capture works without them).
3. Tap **+ Capture** → enter text such as `Submit report by Friday high priority` → **Capture**.
4. Browse **Today**, **Upcoming**, **Notes**, **Calendar**, **All**.
5. Open **Settings** (gear) → **Upgrade** to view Premium products.
6. **In-app purchases (sandbox):**  
   - Product IDs: `premium_lifetime` (non-consumable), `premium_monthly` (auto-renewable subscription).  
   - Both unlock the same Premium set: on-device LLM task parsing (when enabled/downloaded), JSON export, priority support / future Premium features.  
   - Free tier remains fully usable without purchase (rule-based parsing, capture, tasks, notes, summary).  
   - Use a **Sandbox Apple ID** (Users and Access → Sandbox → Testers). On device: Settings → App Store → Sandbox Account.  
   - Tap Settings → Upgrade → choose Lifetime or Monthly.

**Sample content (optional):** Any free-form natural language string works; no sample files are required.

**Notifications:** Optional local task reminders / daily summary—device notification permission only if the reviewer enables them.

---

#### 5) External services, tools, and platforms

LocalMind is designed for **offline-first, on-device** operation. Core task/note data is **not** sent to a developer-operated backend (there is none).

| Service / component | Role |
|---------------------|------|
| **Apple StoreKit 2** | In-app purchases and subscription status (`premium_lifetime`, `premium_monthly`). Only Apple payment stack. |
| **Apple Speech / Speech Recognition frameworks** | On-device (or system) speech-to-text when the user uses Voice Capture; system may show speech recognition permission. |
| **Apple Microphone** | Voice capture input. |
| **On-device LLM (optional Premium)** | Optional model download (e.g. via Cactus SDK packaging) for smarter parsing; model file download may use the network once; inference is on-device; capture content is not uploaded to a LocalMind server. |
| **SQLCipher** | Local encrypted SQLite storage at rest. |
| **Apple local notifications** | Optional reminders (no push server operated by us). |

**Not used for core product data:** No developer-operated auth, no analytics SDK, no ad network, no third-party cloud database for user tasks/notes.

**Privacy policy:** https://www.markduenas.com/privacy  
**Support:** as listed in App Store Connect / support URL metadata.

---

#### 6) Regional differences

The app **functions consistently across all regions**. Features, free tier, and Premium product IDs are the same worldwide. StoreKit displays localized prices by territory. There is no region-locked content, geo-fencing, or alternate feature set by country.

---

#### 7) Regulated industry / protected third-party material

**Not applicable.** LocalMind is a general consumer productivity / task management app. It does not operate in a highly regulated industry (e.g. banking, healthcare provider, gambling, transportation dispatch), does not require professional licenses, and does not include protected third-party copyrighted catalogs or licensed media libraries that require special authorization to redistribute.

Encryption is used only for **local data-at-rest** (SQLCipher). Export compliance: `ITSAppUsesNonExemptEncryption = false` (exempt local encryption use).

---

#### Additional IAP summary for reviewers

- App is **Free**.  
- Premium is optional: `premium_lifetime` (one-time) or `premium_monthly` (auto-renewable).  
- Free tier: capture, rule-based parsing, tasks, notes, calendar/upcoming, optional local notifications.  
- Premium: on-device LLM parsing, JSON export, priority support.  
- No forced account; all user content stays on device unless the user explicitly uses OS share/export.

---

*End of Notes — keep this file updated for future submissions.*
