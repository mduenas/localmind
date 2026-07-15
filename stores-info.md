# LocalMind — Store Listing Info (for initial release)

Compiled from the existing `fastlane/metadata/` content (already well-written and reusable) plus what's needed to actually submit. Copy-paste sections are marked with `>`. Everything under **Blockers** needs a decision or an external action from you before either store will accept a submission — I can't complete those myself.

Current build at time of writing: **versionName 1.0.40**, Android versionCode 48, iOS build 45.

---

## 0. Blockers — resolve these first

| # | Item | Status | Why it blocks submission |
|---|------|--------|---------------------------|
| 1 | **App display name differs by platform** | ✅ Intentional | Android's launcher label is **"LocalMind"**; iOS's is deliberately **"Local Minder"** (a per-platform naming choice, not a bug). The store *listing* title on both platforms is still **"LocalMind — AI Task Manager"** (see §1) — that's what's searchable, independent of the home-screen label. |
| 2 | **Privacy policy URL** | ❌ Missing | Both stores require a live, hosted privacy policy URL before you can publish (Play: Store Listing + Data Safety; App Store: App Privacy page). Draft text is in §5 — you need to host it somewhere (GitHub Pages, a Notion public page, a one-page site) and put the URL in `fastlane/metadata/ios/en-US/privacy_url.txt` and in the Play Console Store Listing "Privacy policy" field. |
| 3 | **Support URL / contact** | ❌ Missing | App Store requires a support URL; Play Console asks for a contact email. Nothing exists in the repo (`fastlane/metadata/ios/en-US/support_url.txt` is empty, no support email anywhere). Needs a real inbox or page you'll actually monitor. |
| 4 | **Feature graphic (Play only)** | ❌ Missing | Required 1024×500 PNG for the Play Store listing header. Not present in `fastlane/metadata/android/en-US/images/`. `SCREENSHOTS.md` already specifies the spec (app icon + tagline "Capture ideas instantly. AI structures them into tasks.") — needs to actually be designed. |
| 5 | **Play Store hi-res icon** | ✅ Generated, not yet uploaded | `fastlane/metadata/android/en-US/images/icon.png` (512×512) exists in the repo now — you said you'd upload it manually via Play Console. Do that before/alongside this release. |
| 6 | **Content rating questionnaire (Play)** | ❌ Not done | Must be completed inside Play Console (Store presence → App content). Expect "Everyone" given no UGC sharing, no ads, no violence — but only the questionnaire result is authoritative. |
| 7 | **Age rating (App Store)** | ❌ Not done | Same idea in App Store Connect's age rating questionnaire. Expect 4+. |
| 8 | **Data Safety form (Play)** and **App Privacy "nutrition label" (App Store)** | ❌ Not done | Both are manual questionnaires inside each console. See §6 for exactly what to answer and why — there's a real nuance around the voice-capture fallback path worth reading before you fill these in. |
| 9 | **In-app purchase review notes** | Recommended | Apple often wants a reviewer note explaining what Premium unlocks, since the reviewer can't otherwise tell what's gated. Draft included in §7. |

Everything else below is ready to paste as-is.

---

## 1. App identity

| Field | Value |
|---|---|
| Store listing title (both platforms) | **LocalMind — AI Task Manager** |
| Android launcher label | `LocalMind` |
| iOS display name (home screen) | `Local Minder` — intentionally different from Android and from the store listing title |
| Android package / applicationId | `com.markduenas.localmind` |
| iOS bundle identifier | `com.markduenas.localmind.localmind` |
| Category (Play) | Productivity |
| Category (App Store) | Primary: Productivity — Secondary (optional): Utilities |
| Min OS | Android 7.0 (API 24+) · iOS 18.2+ |

---

## 2. Google Play Store listing

All of this already exists in `fastlane/metadata/android/en-US/` and is ready to go — reproduced here for convenience.

**Title** (30 char max — current is 27):
> LocalMind — AI Task Manager

**Short description** (80 char max):
> Capture ideas with voice or text. On-device AI turns them into tasks. 100% offline, fully private.

**Full description:**
> LocalMind is a privacy-first AI task manager that runs entirely on your device. Capture ideas with voice or text — the on-device AI parses them into structured tasks with due dates, priorities, and tags. No cloud. No account. No data ever leaves your phone.
>
> KEY FEATURES
>
> - Quick Capture: Voice or text input turns natural language into tasks instantly
> - On-Device AI: Smart parsing powered by on-device LLM — works without internet
> - 100% Offline: All data stays on your device, encrypted at rest
> - Rule-Based Parsing: Free tier extracts dates, priorities, and tags automatically
> - Task Management: Today view, upcoming tasks, priority sorting
> - Notes: Capture thoughts that aren't tasks
> - Daily Summary: Optional notification to review your day
> - Export: Share tasks as JSON (Premium)
>
> PRIVACY BY DESIGN
>
> LocalMind doesn't just promise privacy — it enforces it by architecture. There are no servers, no accounts, no analytics, and no network calls. Your data is encrypted locally with SQLCipher and never transmitted anywhere.
>
> FREE VS PREMIUM
>
> Free: Full capture, rule-based parsing, task management, notes, daily summary
> Premium: On-device LLM for smarter parsing, JSON export, priority support
>
> Get premium with a one-time purchase or monthly subscription.
>
> BUILT FOR YOU
>
> Whether you're a privacy-conscious professional, a student capturing lecture notes, or someone who just wants a fast, reliable task manager — LocalMind gets out of your way and lets you focus on what matters.

**What's new (first release)** — the existing `changelogs/27.txt` and `28.txt` describe incremental features (IAP, Calendar view) that assume a prior release; for the *first* public release, use something like this instead (save as `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, i.e. `48.txt`):
> Welcome to LocalMind — capture tasks and notes by voice or text, and let on-device AI structure them instantly. Fully offline, fully private, no account required.

**Graphics status:**
- ✅ Phone screenshots (9, real captures at 1080×2400 in `images/phoneScreenshots/` — replaced the old 461×1000 placeholders)
- ✅ Hi-res icon (`images/icon.png`, just added)
- ❌ Feature graphic — missing (Blocker #4)
- ⬜ 7"/10" tablet screenshots — optional, currently empty (`.gitkeep` only)

**One nuance worth knowing:** `deploy-android.sh` passes `--skip_upload_images true --skip_upload_screenshots true --skip_upload_metadata true`, so none of the above gets pushed automatically by `./scripts/deploy-android.sh`. For the *first* real Play Store submission you'll need to either upload all of this manually through Play Console, or temporarily flip those flags to `false` and run a deploy (we discussed this tradeoff earlier for the icon — same logic applies to the rest).

---

## 3. Apple App Store listing

Also already drafted in `fastlane/metadata/ios/en-US/`.

**Name** (30 char max):
> LocalMind — AI Task Manager

**Subtitle** (30 char max):
> Offline AI-Powered Task Capture

**Promotional text** (170 char max, editable anytime without review):
> Capture ideas with voice or text. On-device AI structures them into tasks. 100% offline, fully private.

**Keywords** (100 char max, comma-separated, no spaces needed):
> task,todo,AI,offline,private,voice,capture,productivity,notes,manager

**Description:**
> LocalMind is a privacy-first AI task manager that runs entirely on your device. Capture ideas with voice or text — the on-device AI parses them into structured tasks with due dates, priorities, and tags. No cloud. No account. No data ever leaves your phone.
>
> KEY FEATURES
>
> - Quick Capture: Voice or text input turns natural language into tasks instantly
> - On-Device AI: Smart parsing powered by on-device LLM — works without internet
> - 100% Offline: All data stays on your device, encrypted at rest
> - Rule-Based Parsing: Free tier extracts dates, priorities, and tags automatically
> - Task Management: Today view, upcoming tasks, priority sorting
> - Notes: Capture thoughts that aren't tasks
> - Daily Summary: Optional notification to review your day
> - Export: Share tasks as JSON (Premium)
>
> PRIVACY BY DESIGN
>
> LocalMind doesn't just promise privacy — it enforces it by architecture. There are no servers, no accounts, no analytics, and no network calls. Your data is encrypted locally and never transmitted anywhere.
>
> FREE VS PREMIUM
>
> Free: Full capture, rule-based parsing, task management, notes, daily summary
> Premium: On-device LLM for smarter parsing, JSON export, priority support
>
> Get premium with a one-time purchase or monthly subscription.

**What's New (first release)** — same reasoning as Play; replace the current `release_notes.txt` (which describes IAP as if it's an update) with:
> Welcome to LocalMind — capture tasks and notes by voice or text, and let on-device AI structure them instantly. Fully offline, fully private, no account required.

**Graphics status:**
- ✅ Screenshots (9, real captures at exactly 1290×2796 — the required 6.7"/iPhone 15 Pro Max size — in `fastlane/metadata/ios/en-US/screenshots/`, replaced the old 461×1000 placeholders)
- ⚠️ Only the 6.7" size is captured. App Store Connect can often auto-scale from the largest size (6.7") to smaller required sizes (6.5", 5.5", iPad), but verify at upload time whether additional sizes are still required — see `SCREENSHOTS.md`.

---

## 4. Encryption export compliance (already handled)

`Info.plist` already sets `ITSAppUsesNonExemptEncryption = false`, which is correct: SQLCipher is used solely for local data-at-rest encryption (exempt under EAR 740.17(b)(1)), not for communications. No action needed, but when App Store Connect asks the export compliance question at submission, answer consistent with this (encryption is exempt, standard/local-only use).

---

## 5. Privacy policy — draft text

You need to host this somewhere and link it from both consoles. A single static page is enough (GitHub Pages off this repo would work well and is free).

> **Privacy Policy for LocalMind**
> *Last updated: [DATE]*
>
> LocalMind ("the app") is designed so that your data never leaves your device.
>
> **What we collect:** Nothing. LocalMind has no servers, no user accounts, and no analytics or tracking SDKs. All tasks, notes, and captures you create are stored locally on your device, encrypted at rest using SQLCipher.
>
> **Microphone and speech recognition:** LocalMind requests microphone access to let you capture tasks and notes by voice. On supported devices, speech-to-text happens entirely on-device. If on-device recognition is unavailable, the app falls back to your device's built-in system voice input (e.g. Google's speech recognition service on Android), which may send audio to that provider's servers under its own privacy policy. LocalMind itself never receives, stores, or transmits this audio.
>
> **On-device AI models:** Optional AI parsing models can be downloaded from within the app (Premium feature). This download uses the internet to fetch the model file only — no personal data is sent as part of this process.
>
> **In-app purchases:** Purchases are processed entirely by Apple's App Store or Google Play Billing. LocalMind does not receive or store your payment information.
>
> **Notifications:** If enabled, LocalMind schedules local reminder notifications on your device. No notification content is sent to any server.
>
> **Data deletion:** Because all data is stored locally and encrypted, uninstalling the app or clearing its storage permanently deletes all your data. There is nothing for us to delete on our end because we never had it.
>
> **Contact:** [YOUR SUPPORT EMAIL]

Fill in the two `[...]` placeholders before publishing.

---

## 6. Data Safety (Play) / App Privacy (App Store) — how to answer

The nuance to get right: **LocalMind itself collects nothing**, but the Android `RECORD_AUDIO` fallback path (added recently to handle on-device recognition failures) can hand audio to Google's system speech service, which is technically "third-party data sharing" even though LocalMind's own code never touches that audio.

**Google Play Data Safety form:**
- "Does your app collect or share any of the required user data types?" → **Yes** (because of the voice fallback)
- Data type: **Audio (Voice or sound recordings)** → collected: only when on-device recognition is unavailable → not shared with LocalMind, processed by the OS-level/Google speech service you already have on your phone → not used for advertising → user can decline (deny mic permission / use text capture instead)
- Everything else (location, contacts, financial data beyond purchase processing, personal identifiers, etc.) → **not collected**
- Data is encrypted in transit: **Yes** (system service) / at rest: **Yes** (SQLCipher, local only)
- Data deletion: users can delete all data by uninstalling (no account, no server-side copy)

**Apple App Privacy ("nutrition label"):**
- Likely answer: **"Data Not Collected"** is *not* accurate given the voice fallback — declare **Audio Data** as collected but "not linked to the user" and "not used for tracking," with purpose "App Functionality" only.
- Purchase history: Apple's own IAP handles this; you typically don't need to separately declare it since Apple collects it directly, not your app.

Recommend re-reading this section once before you fill in either questionnaire — the honest, defensible answer here is "we collect essentially nothing, with one disclosed exception for the voice fallback," not a blanket "no data collected," since that could get flagged in review.

---

## 7. In-app purchase reviewer notes (App Store)

Apple's reviewers can't infer what Premium unlocks just from the UI, so add this in App Store Connect under the IAP review notes / App Review Information:

> LocalMind Premium (`premium_lifetime` one-time purchase, or `premium_monthly` auto-renewing subscription) unlocks: (1) on-device LLM-based task parsing instead of the free rule-based parser, (2) JSON export of tasks, (3) priority support. Free tier is fully functional without purchase — capture, rule-based parsing, task/note management, and daily summary notifications all work without Premium. To test Premium, use a sandbox account and tap Settings → Upgrade.

Full product details (pricing schedule, product IDs, base plans) are already documented in `fastlane/metadata/BILLING_SETUP.md`.

---

## 8. Pre-submission checklist

- [ ] Resolve app name inconsistency (§1)
- [ ] Host privacy policy, fill in placeholders, add URL to both consoles and `fastlane/metadata/ios/en-US/privacy_url.txt`
- [ ] Set up support email/page, add to `fastlane/metadata/ios/en-US/support_url.txt` and Play Console
- [ ] Design and upload feature graphic (Play, 1024×500)
- [ ] Upload hi-res icon (Play) — file already generated
- [ ] Complete content rating questionnaire (Play)
- [ ] Complete age rating questionnaire (App Store)
- [ ] Complete Data Safety form (Play) using §6 as a guide
- [ ] Complete App Privacy nutrition label (App Store) using §6 as a guide
- [ ] Add IAP reviewer notes (App Store, §7)
- [ ] Replace `changelogs/28.txt`-style incremental notes with the initial-release copy in §2/§3 for this submission
- [ ] Verify screenshot sizes match each store's required dimensions exactly
- [ ] Final review of encryption export compliance answer at submission (§4)
