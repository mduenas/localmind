# LocalMind Product Specification

**Version**: 2.0
**Date**: February 2026
**Author**: Mark Duenas
**App Name**: LocalMind
**Tagline**: Capture ideas instantly → AI structures them into tasks. 100% offline, fully private.

---

## 1. Overview

- **Category**: Productivity / AI Assistant / Task & Note Manager
- **Platforms**: Android (API 24+), iOS (15+); future desktop JVM
- **Target Users**: Privacy-focused individuals, EU users, power users tired of cloud tools
- **Core Promise**: Voice/text capture → on-device AI → structured local tasks. Zero data leaves device.
- **Monetization**: Freemium
  - Free: Basic capture + rule-based parsing
  - Premium ($29–$49 one-time or ~$5/mo): Full LLM, encryption, advanced exports

---

## 2. Competitive Landscape

### Direct Competitors (Privacy-First Task Management)

| App | Strengths | Weaknesses | LocalMind Advantage |
|-----|-----------|------------|---------------------|
| **Lunatask** | Encrypted, habit tracking, journaling | No on-device AI parsing | AI-powered task extraction |
| **Logseq** | Open-source, linked thinking | Complex for simple task capture | Simpler UX, voice-first |
| **Super Productivity** | Time tracking, dev-focused | No AI, desktop-centric | Mobile-first, AI parsing |
| **Obsidian** | 2700+ plugins, local markdown | Steep learning curve, no AI | Zero-config AI experience |
| **Joplin** | E2E encrypted, self-hosted sync | No task intelligence | Smart task extraction |

### On-Device AI Apps (Reference Implementations)

| App | Tech Stack | Notes |
|-----|------------|-------|
| **NotelyVoice** | KMP + Whisper + SQLDelight + Koin | Direct architecture reference (GPL-3) |
| **Enclave AI** | iOS/macOS native, multi-model | Premium positioning, Mac-first |
| **SmolChat** | Android, GGUF models | Chat-only, no task management |
| **Proton Lumo** | Proton ecosystem | Limited to Proton users |

### Key Differentiators for LocalMind

1. **Voice-to-Task Pipeline**: Not just transcription—AI extracts structured tasks
2. **Cross-Platform KMP**: Single codebase for Android + iOS (competitors are often single-platform)
3. **Offline-First**: No cloud dependency, no account required
4. **Privacy by Architecture**: Data never leaves device, not just "privacy policy"

---

## 3. MVP Features (Detailed)

### 3.1 Quick Capture

**Entry Points:**
- Floating action button (Android overlay / iOS widget)
- Home screen widget (quick text field)
- Share sheet integration (receive text from other apps)
- Notification quick action
- App shortcut / Spotlight integration (iOS)

**Input Modes:**
- Text input (single-line quick, multi-line expanded)
- Voice input with real-time waveform visualization
- Voice input with live transcription preview

**UX Requirements:**
- < 500ms to ready-to-type state from any entry point
- Voice recording auto-stops after 3s silence
- Haptic feedback on capture confirmation
- Visual confirmation animation ("captured" state)

### 3.2 On-Device AI Parsing

**Task Extraction Pipeline:**

```
Raw Input → Preprocessing → LLM Inference → Structured Task → User Review → Storage
```

**Structured Task Schema:**
```kotlin
data class ParsedTask(
    val title: String,           // Extracted action item
    val dueDate: LocalDate?,     // Natural language date parsing
    val dueTime: LocalTime?,     // Optional time component
    val priority: Priority,      // HIGH, MEDIUM, LOW (inferred)
    val tags: List<String>,      // Extracted categories/contexts
    val originalText: String,    // Preserved raw input
    val confidence: Float,       // 0.0-1.0 parsing confidence
    val suggestedEdits: List<String>? // Alternative interpretations
)
```

**LLM Prompt Strategy:**
- System prompt: Task extraction specialist with output schema
- Few-shot examples embedded in prompt for consistency
- Temperature: 0.1-0.3 for deterministic outputs
- Max tokens: 256 (task output is compact)

**Fallback Rule-Based Parser (Free Tier):**
- Regex patterns for dates: "tomorrow", "next Monday", "in 3 days", "Mar 15"
- Priority keywords: "urgent", "important", "ASAP", "whenever"
- Tag extraction: hashtags (#work, #personal) and @mentions
- Action verb detection for title extraction

**Draft Preview UI:**
- Shows extracted fields with inline edit capability
- Confidence indicators (color-coded: green/yellow/red)
- "Re-parse" button to regenerate with LLM
- "Save as-is" to preserve original text without parsing

### 3.3 Local Storage & Views

**Database Schema (SQLDelight):**

```sql
-- Core tables
CREATE TABLE tasks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    original_text TEXT NOT NULL,
    due_date INTEGER,  -- Unix timestamp
    due_time INTEGER,  -- Seconds since midnight
    priority INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 0,  -- 0=pending, 1=completed, 2=archived
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    completed_at INTEGER,
    parsing_confidence REAL
);

CREATE TABLE tags (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    color INTEGER
);

CREATE TABLE task_tags (
    task_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE TABLE captures (
    id TEXT PRIMARY KEY,
    raw_text TEXT NOT NULL,
    audio_path TEXT,  -- Path to audio file if voice capture
    created_at INTEGER NOT NULL,
    processed INTEGER NOT NULL DEFAULT 0
);

-- Indexes for common queries
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created ON tasks(created_at);
```

**Views:**

| View | Query Logic | Sort Order |
|------|-------------|------------|
| **Today** | `due_date = today OR (due_date < today AND status = pending)` | Priority → Due time |
| **Upcoming** | `due_date > today AND due_date <= today + 7` | Due date → Priority |
| **All Tasks** | `status != archived` | Created date (desc) |
| **By Tag** | Join on task_tags | Priority → Due date |
| **Search** | FTS on title + original_text | Relevance score |

**Full-Text Search:**
- SQLite FTS5 extension for fast search
- Index: title, original_text, tag names
- Support prefix matching and phrase search

### 3.4 Daily Summary

**Morning Notification (Configurable, default 8:00 AM):**
- Task count for today
- Top 3 priority items preview
- Overdue task warning
- Deep link to Today view

**Evening Recap (Configurable, default 8:00 PM):**
- Completed count / planned count
- Rescheduled items summary
- Tomorrow preview
- Weekly streak tracking

**Implementation:**
- Android: WorkManager with exact timing
- iOS: Background App Refresh + Local Notifications
- User configurable: time, days, enable/disable

### 3.5 Settings

**AI Configuration:**
- LLM model selection (when multiple available)
- Model download/delete management
- Parse preview toggle (skip review for high-confidence)
- Temperature/creativity slider (advanced)

**Privacy & Security:**
- SQLCipher encryption toggle
- Encryption key setup (biometric or PIN)
- Data export (JSON, Markdown, CSV)
- Data wipe (full reset)

**Notifications:**
- Morning summary toggle + time picker
- Evening recap toggle + time picker
- Reminder notifications for due tasks

**Appearance:**
- Theme: System / Light / Dark
- Accent color selection

---

## 4. Technical Architecture

### 4.1 Tech Stack (Updated)

| Layer | Technology | Rationale |
|-------|------------|-----------|
| **UI** | Compose Multiplatform 1.10+ | 95%+ shared UI code |
| **Navigation** | Compose Navigation (native) | No third-party dependency |
| **State** | Kotlin Coroutines + Flow | Reactive, lifecycle-aware |
| **DI** | Koin 4.x | Lightweight, KMP-native |
| **Database** | SQLDelight 2.x | Type-safe, multiplatform SQL |
| **Encryption** | SQLCipher 4.x | AES-256 at-rest encryption |
| **LLM** | Cactus SDK 1.2+ | KMP-native, sub-50ms TTFT |
| **Speech** | Cactus STT (Whisper) | Unified SDK, on-device |
| **Keystore** | Platform Keystore (expect/actual) | Secure key storage |
| **Testing** | kotlin-test + Turbine | Coroutines testing |

### 4.2 Why Cactus SDK over llama.cpp/Llamatik

| Factor | Cactus SDK | llama.cpp (Llamatik) |
|--------|------------|----------------------|
| KMP Support | Native Kotlin bindings | Requires JNI wrapper |
| Speech-to-Text | Integrated (CactusSTT) | Separate integration |
| Performance | Sub-50ms TTFT, 75 tok/s | ~8-10 tok/s mobile |
| API Design | High-level, idiomatic | Low-level C bindings |
| Maintenance | Y Combinator backed, active | Community maintained |
| Licensing | Free for personal/SMB | MIT (open) |

### 4.3 Module Structure

```
localmind/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/com/markduenas/localmind/
│   │   │   │   ├── App.kt                    # Root composable
│   │   │   │   ├── di/                       # Koin modules
│   │   │   │   │   └── AppModule.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── Database.kt       # SQLDelight driver factory
│   │   │   │   │   │   ├── TaskDao.kt        # Task queries wrapper
│   │   │   │   │   │   └── CaptureDao.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── TaskRepository.kt
│   │   │   │   │       └── CaptureRepository.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Task.kt
│   │   │   │   │   │   ├── Tag.kt
│   │   │   │   │   │   └── Capture.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── ParseCaptureUseCase.kt
│   │   │   │   │       ├── GetTodayTasksUseCase.kt
│   │   │   │   │       └── ExportDataUseCase.kt
│   │   │   │   ├── ai/
│   │   │   │   │   ├── LLMService.kt         # Cactus LLM wrapper
│   │   │   │   │   ├── STTService.kt         # Cactus STT wrapper
│   │   │   │   │   ├── TaskParser.kt         # LLM prompt orchestration
│   │   │   │   │   └── RuleBasedParser.kt    # Fallback parser
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Colors.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── NavGraph.kt
│   │   │   │   │   ├── capture/
│   │   │   │   │   │   ├── CaptureScreen.kt
│   │   │   │   │   │   ├── CaptureViewModel.kt
│   │   │   │   │   │   └── VoiceRecorder.kt
│   │   │   │   │   ├── tasks/
│   │   │   │   │   │   ├── TodayScreen.kt
│   │   │   │   │   │   ├── UpcomingScreen.kt
│   │   │   │   │   │   ├── TaskListViewModel.kt
│   │   │   │   │   │   └── TaskCard.kt
│   │   │   │   │   ├── review/
│   │   │   │   │   │   ├── ParseReviewScreen.kt
│   │   │   │   │   │   └── ParseReviewViewModel.kt
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   └── util/
│   │   │   │       ├── DateTimeUtil.kt
│   │   │   │       └── UUIDUtil.kt
│   │   │   ├── composeResources/
│   │   │   │   ├── values/strings.xml
│   │   │   │   └── drawable/
│   │   │   └── sqldelight/
│   │   │       └── com/markduenas/localmind/
│   │   │           └── LocalMindDb.sq
│   │   ├── androidMain/
│   │   │   ├── kotlin/com/markduenas/localmind/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── LocalMindApplication.kt   # Koin init
│   │   │   │   ├── di/AndroidModule.kt       # Android-specific DI
│   │   │   │   ├── data/DatabaseDriverFactory.android.kt
│   │   │   │   ├── notification/
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   └── SummaryWorker.kt      # WorkManager
│   │   │   │   └── widget/
│   │   │   │       └── CaptureWidget.kt
│   │   │   └── AndroidManifest.xml
│   │   └── iosMain/
│   │       └── kotlin/com/markduenas/localmind/
│   │           ├── MainViewController.kt
│   │           ├── di/IosModule.kt
│   │           ├── data/DatabaseDriverFactory.ios.kt
│   │           └── notification/NotificationHelper.ios.kt
│   └── build.gradle.kts
├── iosApp/
│   └── (SwiftUI wrapper, widgets, extensions)
├── gradle/
│   └── libs.versions.toml
└── build.gradle.kts
```

### 4.4 Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │ Capture  │  │  Today   │  │ Upcoming │  │ Parse Review     │ │
│  │ Screen   │  │  Screen  │  │  Screen  │  │ Screen           │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────────┬─────────┘ │
│       │             │             │                  │           │
│       ▼             ▼             ▼                  ▼           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    ViewModels (StateFlow)                 │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Domain Layer                               │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │ ParseCaptureUseCase│  │GetTodayTasksUseCase│                  │
│  └────────┬─────────┘  └────────┬─────────┘                     │
│           │                      │                               │
└───────────┼──────────────────────┼───────────────────────────────┘
            │                      │
            ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │ TaskRepository│  │CaptureRepository│  │ AIService (Cactus)   │ │
│  └───────┬──────┘  └───────┬──────┘  │  ├─ LLMService          │ │
│          │                  │         │  └─ STTService          │ │
│          ▼                  ▼         └───────────┬────────────┘ │
│  ┌──────────────────────────────────┐             │              │
│  │  SQLDelight (+ SQLCipher)        │             │              │
│  │  ├─ TaskDao                      │◄────────────┘              │
│  │  └─ CaptureDao                   │                            │
│  └──────────────────────────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Non-Functional Requirements

### 5.1 Performance Targets

| Metric | Target | Measurement |
|--------|--------|-------------|
| App cold start | < 2s | Time to interactive |
| Capture to ready | < 500ms | FAB tap to cursor |
| Voice transcription | < 2s for 10s audio | End-to-end |
| LLM parse time | < 3s | Input to structured output |
| Task list scroll | 60 FPS | No frame drops |
| Memory peak (LLM) | < 1.5 GB | During inference |
| Memory idle | < 100 MB | App backgrounded |
| APK size | < 50 MB | Without models |
| Model size | ~300-600 MB | qwen3-0.6 quantized |

### 5.2 Privacy & Security

- **Zero Network**: App functions with no internet permission (except model download)
- **No Telemetry**: No analytics, crash reporting, or usage tracking
- **No Account**: No sign-up, login, or cloud sync required
- **Local Only**: All data stored on-device
- **Optional Encryption**: SQLCipher AES-256 for at-rest encryption
- **Secure Keys**: Encryption keys in platform Keystore (biometric protected)
- **Export Only**: User-initiated export (no sync = no data leak vectors)

### 5.3 Accessibility

- **Screen Reader**: TalkBack (Android) / VoiceOver (iOS) support
- **Scaling**: Respect system font size preferences
- **Contrast**: WCAG AA minimum contrast ratios
- **Touch Targets**: Minimum 48dp interactive elements
- **Reduce Motion**: Honor system animation preferences

---

## 6. Model Strategy

### 6.1 Recommended Models

**Primary LLM (via Cactus SDK):**
- **qwen3-0.6** (600M params, ~400MB quantized) - Default
- Excellent instruction-following for task extraction
- 40-75 tok/s on modern phones

**Alternative LLM:**
- **gemma3-270m** (270M params, ~200MB) - Faster, lower quality
- Good for older devices or quick responses

**Speech-to-Text:**
- **whisper-tiny** (~75MB) - Fast, good accuracy for clear speech
- **whisper-base** (~150MB) - Better accuracy, 2x slower

### 6.2 Model Management

- Models downloaded on-demand (not bundled)
- Progress indicator during download
- Models stored in app-private storage
- Delete model option in settings
- Automatic cleanup on app uninstall

### 6.3 Prompt Engineering

**Task Extraction System Prompt:**
```
You are a task extraction assistant. Given raw user input, extract structured task information.

Output JSON only, no explanation:
{
  "title": "concise action item",
  "due_date": "YYYY-MM-DD or null",
  "due_time": "HH:MM or null",
  "priority": "high|medium|low",
  "tags": ["tag1", "tag2"]
}

Rules:
- Title should start with action verb when possible
- Infer dates from "tomorrow", "next week", "Friday", etc.
- Default priority is "medium" unless urgency indicated
- Extract hashtags as tags, infer categories from context
- If ambiguous, prefer null over guessing
```

**Few-Shot Examples (embedded):**
```
Input: "call mom tomorrow about birthday party"
Output: {"title":"Call mom about birthday party","due_date":"2026-02-13","due_time":null,"priority":"medium","tags":["family"]}

Input: "URGENT fix prod bug in auth service #work"
Output: {"title":"Fix production bug in auth service","due_date":null,"due_time":null,"priority":"high","tags":["work"]}
```

---

## 7. Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Model too slow on old devices | Medium | High | Default to rule-based parser; device compatibility check |
| High battery drain | Medium | Medium | Batch inference; short bursts; show battery warning |
| App Store rejection | Low | High | No external code execution; emphasize local-only |
| Model hallucination | Medium | Low | Low temperature; confidence scores; user review step |
| Large app size | Medium | Medium | On-demand model download; show storage requirements |
| SQLCipher key loss | Low | High | Biometric + PIN backup; key recovery warning |

---

## 8. Future Roadmap (Post-MVP)

### Phase 2: Enhanced Productivity
- Recurring tasks
- Subtasks / checklists
- Calendar integration (local only)
- Task templates

### Phase 3: Intelligence
- Smart scheduling suggestions
- Task prioritization AI
- Natural language task queries ("show me urgent work tasks")
- Daily planning assistant

### Phase 4: Platform Expansion
- Desktop JVM (macOS, Windows, Linux)
- Wear OS / watchOS quick capture
- Android Auto / CarPlay integration

### Phase 5: Optional Sync
- E2E encrypted sync (user-hosted or Proton Drive)
- Device-to-device direct sync
- Backup/restore

---

## 9. Success Metrics

**MVP Launch (Week 8):**
- [ ] Capture → Parse → Store flow works end-to-end
- [ ] Voice transcription functional on both platforms
- [ ] Today/Upcoming views populated correctly
- [ ] No crashes on reference devices
- [ ] < 3s parse time on Pixel 6 / iPhone 12

**Post-Launch (Month 3):**
- 1000+ downloads
- 4.0+ app store rating
- < 1% crash rate
- 80%+ task parsing accuracy (user feedback)

---

## Appendix A: Reference Implementations

- **NotelyVoice** (GPL-3): https://github.com/tosinonikute/NotelyVoice
- **Cactus SDK Kotlin**: https://github.com/cactus-compute/cactus-kotlin
- **SQLDelight + SQLCipher**: https://touchlab.co/multiplatform-encryption-with-sqldelight-and-sqlcipher
- **WhisperKit**: https://github.com/argmaxinc/WhisperKit

## Appendix B: Device Compatibility Matrix

| Device Class | RAM | LLM Model | STT Model | Expected Performance |
|--------------|-----|-----------|-----------|---------------------|
| Flagship 2024+ | 12GB+ | qwen3-0.6 | whisper-base | Excellent |
| Mid-range 2023+ | 8GB | qwen3-0.6 | whisper-tiny | Good |
| Budget 2022+ | 6GB | gemma3-270m | whisper-tiny | Acceptable |
| Older/Low-end | 4GB | Rule-based only | Platform STT | Functional |
