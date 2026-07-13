# LocalMind Simply — Implementation Plan (Capture-First, Fast AI)

**Document purpose:** Hand this file to an implementation model. Follow it step by step. Do not redesign the whole app. Optimize for **instant capture** and **reliable background classification** into note / task / scheduled task.

**Product name:** LocalMind  
**Repo:** Kotlin Multiplatform (Android + iOS), package `com.markduenas.localmind`  
**Date of plan:** 2026-07-13  
**Primary problem:** On-device Cactus LLM inference is too slow for a capture-first UX.  
**Primary goal:** Make note/task capture feel instant. AI may finish later.

---

## 0. Instructions for the implementing model

1. Read this entire document before writing code.
2. Prefer **small, shippable PRs** matching the phases below.
3. Do **not** remove encryption, SQLDelight, Koin, or the offline/privacy promise.
4. Do **not** send user content to cloud APIs. Platform on-device models (Apple Foundation Models on-device path; Android Gemini Nano / AICore) are allowed only when they stay on-device.
5. Keep free-tier rule-based parsing working forever as the universal fallback.
6. When unsure, choose the option that makes **time-to-save-raw-capture** smaller.
7. After each phase: compile Android debug, run unit tests, and smoke-test voice + text capture.
8. Do not expand scope into widgets, paywall redesign, calendar polish, or new product surfaces unless a phase explicitly requires it.

### Success criteria (must hit)

| Metric | Target |
|--------|--------|
| Tap mic → recording ready | < 300 ms |
| Stop speaking / submit text → raw capture saved + UI confirms | < 200 ms |
| User free to leave capture screen | Immediately after confirmation |
| Background classify + create note/task | < 2 s P50 when platform AI available; < 5 s P95 |
| Rule-based only path (no platform AI) | < 50 ms classify + save |
| Offline | Full capture + classify works offline |
| Privacy | No cloud LLM for capture content |

---

## 1. Product intent (what we are building)

### 1.1 User story (north star)

> I open the app (or hit the capture button), speak or type, and I'm done. The app figures out whether that was a note, a todo, or a scheduled task. I don't wait on a spinner for AI.

### 1.2 Capture outputs (exactly 3 categories)

Given raw text (from speech or typing), the system must produce **one** of:

1. **Note** — idea, observation, quote, freeform thought  
   - Fields: `title`, `body`, `tags`, `originalText`, `confidence`
2. **Task (no schedule)** — action item with no due date/time  
   - Fields: `title`, `priority`, `tags`, `originalText`, `confidence`, `dueDate=null`, `dueTime=null`
3. **Task (with schedule)** — action item with due date and/or time  
   - Same as task, but `dueDate` and/or `dueTime` populated

There is **no fourth type**. "Todo" == task. Scheduling is a field on task, not a separate entity.

### 1.3 UX principle: capture first, intelligence second

```
BEFORE (current pain):
  Speak → wait for STT → navigate to Review → wait for rule parse → maybe wait 2–7s for LLM → edit → save

AFTER (target):
  Speak/type → instant "Captured ✓" → raw text stored → user returns to list
  (background) classify → create Note or Task → list updates when ready
  (optional) user can open item later to fix fields
```

**Default path:** auto-save after classification (no mandatory review screen).  
**Review screen:** keep for edit/correction, not as a required step before save.

---

## 2. Current codebase inventory (facts — do not invent APIs)

### 2.1 Stack

| Layer | Tech |
|-------|------|
| UI | Compose Multiplatform 1.10 |
| DI | Koin 4.x |
| DB | SQLDelight 2 + SQLCipher (always encrypted) |
| LLM (current premium) | Cactus SDK 1.4.1-beta (`gemma3-270m`, `qwen3-0.6`, optional `gemma3-1b`) |
| STT | Already platform-native (not Cactus Whisper) |
| Background | `androidx.work` on Android; expect/actual `BackgroundTaskRunner` |
| Min versions | Android API 24+, iOS 15+ (raise only when a phase requires it and document why) |

### 2.2 Important source files

| Area | Path |
|------|------|
| LLM wrap | `composeApp/src/commonMain/kotlin/.../ai/LLMService.kt` |
| Prompt + JSON schema | `.../ai/Prompts.kt`, `.../ai/JsonParser.kt` |
| LLM orchestration | `.../ai/TaskParser.kt` |
| Fast regex parser | `.../ai/RuleBasedParser.kt` (+ Date/Time/Priority patterns) |
| Parse use case | `.../domain/usecase/ParseCaptureUseCase.kt` |
| Domain outputs | `.../domain/model/ParsedCapture.kt`, `ParsedTask.kt`, `ParsedNote.kt` |
| Capture model | `.../domain/model/Capture.kt` |
| Capture VM | `.../ui/capture/CaptureViewModel.kt` |
| Review VM (blocking enhance) | `.../ui/review/ParseReviewViewModel.kt` |
| Nav | `.../ui/navigation/NavGraph.kt` |
| Speech expect | `.../platform/SpeechRecognitionService.kt` |
| Speech Android | `.../androidMain/.../SpeechRecognitionService.android.kt` (on-device when available) |
| Speech iOS | `.../iosMain/.../SpeechRecognitionService.ios.kt` (SFSpeechRecognizer on-device) |
| Schema | `composeApp/src/commonMain/sqldelight/.../LocalMindDb.sq` |
| AI config | `.../ai/AIConfig.kt` |
| DI | `.../di/AppModule.kt` + platform modules |

### 2.3 Existing parse pipeline (today)

`ParseCaptureUseCase`:
1. `parseImmediate` → always `RuleBasedParser` (milliseconds).
2. `parseEnhancement` → if premium + LLM enabled + not "simple" text → Cactus LLM (seconds).
3. Review UI waits for enhancement (`isEnhancing`) before user is fully done.

**Problem:** Even with rule-first, the review flow and LLM enhance make capture feel slow. Benchmarks (2026-03-20) show rule-based **wins quality** vs current small Cactus models and is ~1000–3000× faster:

| Model | Composite quality | P50 latency |
|-------|-------------------|-------------|
| rule-based | **0.96** | **~2 ms** |
| gemma3-270m | ~0.70 | ~1.8–3.6 s |
| qwen3-0.6 | ~0.66 | ~3.9–6.7 s |
| gemma3-1b | ~0.74 | ~5 s |

Conclusion for implementers: **do not optimize Cactus first.** Change UX + use OS-native AI where available. Keep Cactus as optional last-resort backend only if still needed after platform adapters.

### 2.4 Schema already has captures table

```sql
CREATE TABLE captures (
    id TEXT NOT NULL PRIMARY KEY,
    raw_text TEXT NOT NULL,
    audio_path TEXT,
    created_at INTEGER NOT NULL,
    processed INTEGER NOT NULL DEFAULT 0
);
```

Queries already exist: `insertCapture`, `getUnprocessedCaptures`, `markCaptureProcessed`.  
**Use this table** as the queue. Extend it; do not invent a parallel queue.

### 2.5 STT is already platform-native

Do not reintroduce Cactus Whisper. Improve recording UX and ensure offline recognition flags stay on.

---

## 3. Target architecture

### 3.1 Mental model

```
┌─────────────────────────────────────────────────────────────┐
│ CAPTURE SURFACE (must be instant)                           │
│  Voice | Text | Share sheet | Widget                         │
│  → Persist Capture row (processed=0)                        │
│  → Haptic + "Captured" toast/snackbar                       │
│  → Pop capture screen                                       │
└───────────────────────────┬─────────────────────────────────┘
                            │ enqueue
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ CAPTURE PROCESSOR (background, durable)                     │
│  1. Load unprocessed captures                               │
│  2. Classify with CaptureIntelligence (interface)           │
│  3. Create Note OR Task via existing use cases              │
│  4. markCaptureProcessed=1                                  │
│  5. Emit list refresh / badge "3 processing…"               │
└───────────────────────────┬─────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
   PlatformNativeAI    RuleBasedParser    CactusLLM (optional)
   (Apple / Google)    (always available) (legacy / weak devices)
```

### 3.2 New core abstraction (commonMain)

Create a single interface that all backends implement:

```kotlin
// composeApp/src/commonMain/kotlin/com/markduenas/localmind/ai/CaptureIntelligence.kt
package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture

interface CaptureIntelligence {
    /** Human-readable id for logs: "rule", "apple-fm", "gemini-nano", "cactus", etc. */
    val id: String

    /** False if model/API unavailable on this device right now. */
    suspend fun isAvailable(): Boolean

    /**
     * Classify raw capture text into Note or Task (with optional schedule).
     * Must not throw for expected failures — return Result.failure instead.
     */
    suspend fun classify(rawText: String, todayIsoDate: String): Result<ParsedCapture>
}
```

### 3.3 Orchestrator (commonMain)

```kotlin
// CaptureIntelligenceRouter.kt
class CaptureIntelligenceRouter(
    private val preferred: List<CaptureIntelligence>, // ordered by preference
    private val ruleBased: CaptureIntelligence,       // always last fallback
) : CaptureIntelligence {
    override val id = "router"
    override suspend fun isAvailable() = true

    override suspend fun classify(rawText: String, todayIsoDate: String): Result<ParsedCapture> {
        for (engine in preferred) {
            if (!engine.isAvailable()) continue
            val result = engine.classify(rawText, todayIsoDate)
            if (result.isSuccess) return result
        }
        return ruleBased.classify(rawText, todayIsoDate)
    }
}
```

**Default engine order:**
1. Platform native (Apple Foundation Models / Android ML Kit GenAI or AICore) when available  
2. Rule-based (always)  
3. Cactus LLM only if settings enable it AND platform native unavailable AND premium — **not required for MVP of this plan**

### 3.4 Classification contract (JSON — keep stable)

All engines that use generative AI must target this schema (already used in `Prompts.kt` / `JsonParser.kt`):

```json
{
  "type": "task" | "note",
  "title": "string",
  "body": "string or null",
  "due_date": "YYYY-MM-DD or null",
  "due_time": "HH:MM or null",
  "priority": "low" | "medium" | "high",
  "tags": ["string"],
  "confidence": 0.0
}
```

Rules:
- `type=note` → `due_date` and `due_time` must be null; `body` should be original or cleaned text.
- `type=task` → `body` usually null; title is imperative short form.
- Relative dates resolve using `today_date`.
- Prefer reusing `JsonParser.parse(response, rawText)` rather than writing a second parser.

### 3.5 Three-way mapping after classify

```kotlin
when (val capture = parsed) {
    is ParsedCapture.NoteCapture -> createNoteUseCase(...)
    is ParsedCapture.TaskCapture -> createTaskUseCase(...) // dueDate/time optional
}
```

No new domain types needed for "todo" vs "scheduled task".

---

## 4. Platform AI strategy

### 4.1 iOS — Apple Foundation Models (on-device)

**What:** Apple Foundation Models framework (Apple Intelligence on-device ~3B model via `SystemLanguageModel` / `LanguageModelSession`). Structured / guided generation is preferred when available.

**Constraints:**
- Requires Apple Intelligence–capable device + user enabled Apple Intelligence.
- Framework availability is OS-gated (iOS 26+ era). Older iOS must fall back to rule-based (and optional Cactus).
- Call **only** the on-device system model for LocalMind. Do not send capture text to Private Cloud Compute unless product later explicitly opts in with a clear privacy disclosure. Default = on-device only.

**Integration pattern in KMP:**
1. Implement thin **Swift** helper in `iosApp/` (easiest for FoundationModels API).
2. Expose a C-compatible or Kotlin/Native-callable bridge, OR call from `iosMain` Kotlin via carefully wrapped interop if feasible.
3. Recommended: Swift class `CaptureClassifier` with method `classify(text:today:completion:)`, bridge to Kotlin `actual class PlatformCaptureIntelligence`.

**Prompt for Apple FM (keep short):**

```
You classify personal productivity captures.
Today is {today}.
Return ONLY JSON with keys: type,title,body,due_date,due_time,priority,tags,confidence.
type is "task" or "note".
Tasks are actions; notes are ideas/observations.
due_date YYYY-MM-DD or null; due_time HH:MM 24h or null.
```

If guided generation / `@Generable` structured output is available, define a Swift struct matching the schema and use it — more reliable than freeform JSON.

**Availability check:** If model unavailable, `isAvailable() = false` and router skips to rule-based.

### 4.2 Android — Google on-device GenAI

**Preferred order of attempts:**

1. **ML Kit GenAI APIs** (high-level, Gemini Nano via AICore) when APIs fit classification / prompt use case.  
2. **AICore / Gemini Nano prompt API** (developer preview / production as available) for custom prompt → JSON.  
3. Else rule-based.

**Constraints:**
- AICore / Gemini Nano are **device-dependent** (not all phones). Always feature-detect.
- Shared system model (no multi-hundred-MB download in-app for Nano) when present — good for UX.
- Keep minSdk 24; wrap all GenAI calls in SDK version + availability checks.
- Use background-safe APIs (ML Kit documents background usage — respect their rules).

**Integration pattern:**
```
androidMain actual class PlatformCaptureIntelligence(context: Context) : CaptureIntelligence
```

Use coroutines; never block main thread.

**Prompt:** same JSON contract as iOS.

### 4.3 Rule-based engine (mandatory)

Wrap existing `RuleBasedParser` as `RuleBasedCaptureIntelligence`:
- `isAvailable() = true` always
- `classify` calls `RuleBasedParser.parse(rawText)`
- Map to `Result.success`

Improve rules only after platform path works (Phase 5).

### 4.4 Cactus (optional / de-emphasized)

Do **not** block the new UX on Cactus.
- Keep `LLMService` / model download UI for now so premium users on weak OS versions still have a path.
- Wire Cactus as lowest-priority generative engine in the router only if settings `llmEnabled` and premium.
- Long-term: may remove model download entirely if platform coverage is good enough. Out of scope until Phases 1–4 ship.

---

## 5. Capture UX redesign (detailed)

### 5.1 New happy path

1. User opens Capture (FAB / nav / widget / share).
2. User speaks or types.
3. On final speech result **or** submit:
   - Generate UUID for capture.
   - Insert into `captures` with `processed=0`, `raw_text`, optional `audio_path`, `created_at`.
   - Show confirmation (snackbar / checkmark animation). Haptic if available.
   - Navigate back immediately (do **not** go to ParseReview by default).
4. Enqueue background processing for that capture id.
5. When processing completes, Today / Notes / Upcoming lists refresh via existing Flows.
6. Optional: temporary "Processing…" chip on home, count of unprocessed captures.

### 5.2 What to change in navigation

**Current:** `Capture` → `ParseReview(captureText)` → save → lists.

**Target:** `Capture` → save raw → pop to previous / Today.  
Keep route `ParseReview` for:
- Tapping a **processing error** item
- Settings toggle "Review before save" (optional advanced; implement only if quick)
- Editing after the fact from task/note detail (already exists)

Minimal change for Phase 1:  
In `NavGraph.kt` Capture `onSubmit`, call a new use case `EnqueueCaptureUseCase` instead of navigating to ParseReview.

### 5.3 Voice specifics

- Keep platform speech services.
- On final result, auto-enqueue (CaptureViewModel already has `autoSubmit` SharedFlow — wire it to enqueue, not review).
- If speech error, show error; do not save empty capture.
- Optional later: save audio file path into `captures.audio_path` for re-transcription; not required for first ship.

### 5.4 Text specifics

- Submit button enqueues and clears field.
- Empty submit ignored.

### 5.5 Share sheet / widget

- Android `ShareReceiverActivity` already inserts a Capture — ensure it also enqueues processing (today it may only insert).
- Widget deep-link should open capture or accept text and enqueue.

---

## 6. Background processing (durable)

### 6.1 Extend schema

Add columns to `captures` (migration via SQLDelight):

```sql
-- conceptual new columns
status TEXT NOT NULL DEFAULT 'pending',  -- pending | processing | done | failed
result_type TEXT,                        -- task | note | null
result_id TEXT,                          -- created task/note id
error_message TEXT,
engine_used TEXT,                        -- rule | apple-fm | gemini-nano | cactus
processed_at INTEGER
```

Migration notes:
- Keep `processed INTEGER` for backward compatibility OR migrate `processed=1` → `status='done'`.
- Prefer one source of truth: if you add `status`, derive `processed` as `status == 'done'` in mappers, or stop using `processed` after migration. Document choice in code comments.

Suggested approach for less risk:
1. Add new columns with defaults.
2. `getUnprocessedCaptures` becomes `WHERE status IN ('pending','failed')` with retry limit later.
3. Leave old column updated in parallel (`processed=1` when done) so old queries don't break.

### 6.2 Use cases

```kotlin
class EnqueueCaptureUseCase(
    private val captureRepository: CaptureRepository,
    private val processor: CaptureProcessor,
) {
    suspend operator fun invoke(rawText: String, audioPath: String? = null): String {
        val id = /* uuid */
        captureRepository.insert(Capture(id, rawText.trim(), audioPath, now, processed = false))
        processor.schedule(id) // fire-and-forget
        return id
    }
}

class ProcessCaptureUseCase(
    private val captureRepository: CaptureRepository,
    private val intelligence: CaptureIntelligence,
    private val createTask: CreateTaskUseCase,
    private val createNote: CreateNoteUseCase,
) {
    suspend operator fun invoke(captureId: String) { /* see steps below */ }
}
```

**ProcessCaptureUseCase steps:**
1. Load capture; if missing or already done, return.
2. Mark `status=processing`.
3. `intelligence.classify(rawText, today)`.
4. On success:
   - If note → `CreateNoteUseCase`
   - If task → `CreateTaskUseCase`
   - Set `result_type`, `result_id`, `engine_used`, `status=done`, `processed=1`, `processed_at`
5. On failure:
   - Run rule-based once more if not already used
   - If still fail: `status=failed`, store `error_message`, keep raw text for user recovery

### 6.3 Platform scheduling

**Android:** WorkManager one-time work per capture id (or a single worker that drains the queue).  
- Constraints: none required for on-device; do not require network.  
- Expedited work if appropriate for user-initiated capture.  
- Existing dependency: `androidx.work:work-runtime-ktx`.

**iOS:**  
- Process immediately in app via background coroutine when app is foregrounded.  
- Optionally BGTaskScheduler for later; first version can process as soon as enqueued while app is alive.  
- On next cold start, drain `getUnprocessedCaptures` in app init (Koin / App.kt).

**commonMain:** `CaptureProcessor` expect/actual:
```kotlin
expect class CaptureProcessor {
    fun schedule(captureId: String)
    fun scheduleDrainAll()
}
```

### 6.4 Startup drain

On app launch (both platforms), call `scheduleDrainAll()` so pending captures never strand.

---

## 7. UI status & recovery

### 7.1 Processing indicator

- In Today or root scaffold: if `unprocessedCount > 0`, show subtle banner: `"Processing 2 captures…"`.
- When a capture fails: `"1 capture needs attention"` tappable → simple list of failed raw texts with Retry / Convert to note / Delete.

### 7.2 Failed capture recovery screen (minimal)

- List failed captures: raw text, error, time.
- Actions: Retry processing | Save as note (force) | Delete.

### 7.3 Do not block lists

Tasks/notes created in background should appear via existing repository Flows. Verify `TaskRepository` / `NoteRepository` expose reactive queries; if not, add `Flow` observers or invalidate.

---

## 8. Phased implementation plan

Do phases in order. Each phase ends with a checklist.

---

### Phase 0 — Documentation & guardrails (no product change)

**Goal:** Align repo docs; freeze behavior assumptions.

**Tasks:**
1. Skim `localmind-specification.md` sections on capture + AI; note conflicts with this plan (spec still says Cactus STT — ignore that, code already uses platform STT).
2. Add a short comment at top of `ParseCaptureUseCase.kt` pointing to this plan's target (optional).

**Done when:** Team (or you) agree this file is the source of truth for the speed redesign.

---

### Phase 1 — Instant raw capture (no AI change yet)

**Goal:** Capture never waits on LLM. Raw text always saved first.

**Tasks:**
1. Add `EnqueueCaptureUseCase` as above using existing `CaptureRepository.insertCapture`.
2. Change `CaptureScreen` / `NavGraph` so submit + voice `autoSubmit` call enqueue and pop back — **skip ParseReview**.
3. Show snackbar: `"Captured"`.
4. On app start, if any unprocessed captures exist, process them with **rule-based only** via a simple `ProcessCaptureUseCase` that only uses `RuleBasedParser` + create note/task. (Platform AI comes in Phase 3.)
5. Update share receiver to trigger process after insert.
6. Leave ParseReview accessible only via debug or temporary deep link if needed for QA.

**Files likely touched:**
- `domain/usecase/EnqueueCaptureUseCase.kt` (new)
- `domain/usecase/ProcessCaptureUseCase.kt` (new)
- `ui/capture/CaptureViewModel.kt`
- `ui/capture/CaptureScreen.kt`
- `ui/navigation/NavGraph.kt`
- `di/AppModule.kt`
- Android `ShareReceiverActivity.kt`
- App startup (`App.kt` or platform Application / iOS bridge)

**Tests:**
- Unit test: enqueue inserts `processed=0`.
- Unit test: process creates task for `"buy milk tomorrow"` and note for observation-like text (use existing fixture ideas from `ParserBenchmarkTest` / `BenchmarkFixtures`).
- Manual: type capture → back to Today within 200 ms feel; item appears after process.

**Done when:** User never sees ParseReview on the default path; rule-based background create works.

---

### Phase 2 — Schema + durable queue + status UI

**Goal:** Reliable queue with status and failure recovery.

**Tasks:**
1. Extend `LocalMindDb.sq` captures table with status/result/error/engine fields.
2. Update mappers + `CaptureRepository` / impl.
3. Implement WorkManager worker (Android) + foreground drain (iOS).
4. Startup `scheduleDrainAll()`.
5. Banner for processing count + failed count.
6. Minimal Failed Captures UI.

**Done when:** Kill app mid-process; relaunch drains queue. Failed items retryable.

---

### Phase 3 — CaptureIntelligence abstraction + rule engine adapter

**Goal:** Pluggable AI backends without UI rewrites.

**Tasks:**
1. Add `CaptureIntelligence` interface.
2. Implement `RuleBasedCaptureIntelligence`.
3. Implement `CaptureIntelligenceRouter`.
4. Point `ProcessCaptureUseCase` at router.
5. Optionally wrap Cactus `TaskParser` as `CactusCaptureIntelligence` behind settings flag (off by default for speed).
6. Log `engine_used` on each capture.

**Done when:** Processing goes only through the interface; rule path still green on all tests.

---

### Phase 4 — Platform native AI (the speed/quality upgrade)

**Goal:** Use Apple / Google on-device models when present.

#### 4A — Android

1. Research current stable artifacts for ML Kit GenAI / AICore prompt APIs at implementation time (versions change — verify docs).
2. Add dependencies only in `androidMain` / android gradle config.
3. Implement `PlatformCaptureIntelligence` actual for Android:
   - availability check
   - prompt with JSON schema
   - parse via shared `JsonParser`
4. Feature-detect: if unavailable, router skips.
5. Test on an AICore-capable device/emulator if possible; otherwise test skip path.

#### 4B — iOS

1. Raise deployment target only if required by FoundationModels (document in PR).
2. Add Swift `CaptureClassifier` using Foundation Models on-device session.
3. Prefer structured output APIs if available.
4. Bridge to Kotlin `actual class PlatformCaptureIntelligence`.
5. If Apple Intelligence off/unavailable → `isAvailable()=false`.

#### 4C — Wiring

1. In DI, build preferred list: `[platform, cactus?, rule]`.
2. Never call platform AI on main thread.
3. Timeouts: e.g. 8s platform, then fall back to rule.

**Done when:** On a supported device, capture → background classify uses platform engine_id; latency P50 < 2s for short utterances. On unsupported device, rule path unchanged.

---

### Phase 5 — Quality & speed polish

**Goal:** Better classification without hurting instant capture.

**Tasks:**
1. Expand rule-based edge cases using benchmark fixtures (`commonTest/.../BenchmarkFixtures.kt`).
2. Tune platform prompts with 5–10 few-shot examples (keep prompt short).
3. Fast-path: if text is very short and rule-based confidence heuristics strong, skip generative AI (similar to current `shouldSkipEnhancement` but inverted: generative only when rule is uncertain).
4. Optional: confidence threshold — if platform confidence < 0.5, prefer rule or mark for review.
5. Remove mandatory model download friction from first-run if platform AI covers the device; keep settings entry for power users.

**Heuristic for "rule is enough" (example):**
- word count ≤ 10 AND (has clear date pattern OR has action verb OR explicit `#tag`) AND no complexity markers → rule only.

**Done when:** Benchmark suite quality ≥ rule baseline; platform AI improves hard cases without regressing easy ones.

---

### Phase 6 — Optional cleanup

Only after Phases 1–5:

1. Make ParseReview an editor for existing tasks/notes, not a pre-save gate.
2. Deprecate Cactus download for devices with platform AI.
3. Update `localmind-specification.md` and store metadata to say platform on-device AI + rule fallback (still 100% offline on supported paths).
4. Revisit premium: value may shift from "LLM download" to "advanced intelligence / export / themes" — product decision, not required for this plan.

---

## 9. Concrete coding recipes

### 9.1 Enqueue from CaptureViewModel (pattern)

```kotlin
// Pseudocode — adapt to existing state patterns
fun submitAndEnqueue() {
    val text = _uiState.value.inputText.trim()
    if (text.isEmpty()) return
    viewModelScope.launch {
        enqueueCapture(text)
        _events.emit(CaptureEvent.Captured)
        _uiState.update { it.copy(inputText = "") }
    }
}
```

NavGraph listens for `Captured` → `popBackStack()`.

### 9.2 ProcessCaptureUseCase (core logic sketch)

```kotlin
suspend operator fun invoke(captureId: String) {
    val capture = repo.getById(captureId) ?: return
    if (capture.processed) return

    repo.markProcessing(captureId)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    val parsed = intelligence.classify(capture.rawText, today)
        .getOrElse {
            ruleBased.classify(capture.rawText, today).getOrElse { err ->
                repo.markFailed(captureId, err.message)
                return
            }
        }

    when (parsed) {
        is ParsedCapture.NoteCapture -> {
            val id = createNote(parsed.note)
            repo.markDone(captureId, resultType = "note", resultId = id, engine = intelligence.id)
        }
        is ParsedCapture.TaskCapture -> {
            val id = createTask(parsed.task)
            repo.markDone(captureId, resultType = "task", resultId = id, engine = intelligence.id)
        }
    }
}
```

Adapt `createNote` / `createTask` to existing use case signatures (`CreateNoteUseCase`, `CreateTaskUseCase`).

### 9.3 WorkManager skeleton (Android)

```kotlin
class ProcessCaptureWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        return try {
            processCapture(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

Inject use case via Koin entry point or custom WorkerFactory (match project patterns).

### 9.4 Do not break encryption

All DB access stays through existing SQLDelight driver + SQLCipher key provider. No second database for the queue.

---

## 10. Testing plan

### 10.1 Unit tests (commonTest)

| Test | Expect |
|------|--------|
| Rule engine: `"Buy milk tomorrow"` | Task + dueDate = tomorrow |
| Rule engine: `"Interesting article about sleep"` | Note |
| Rule engine: `"Call dentist Friday at 3pm"` | Task + date + time |
| Router: preferred fails → rule succeeds | result from rule, no throw |
| ProcessCapture: note path marks processed + inserts note | DB side effects via fakes |
| ProcessCapture: empty text → failed or skipped | no crash |
| Enqueue: blank text rejected | no insert |

Reuse fixtures from:
- `composeApp/src/commonTest/kotlin/com/markduenas/localmind/ai/ParserBenchmarkTest.kt`
- `composeApp/src/commonTest/kotlin/com/markduenas/localmind/ai/benchmark/BenchmarkFixtures.kt`

### 10.2 Instrumented / manual

1. Cold start → mic → speak short task → leave screen immediately → task appears within a few seconds.  
2. Airplane mode → still works.  
3. Unsupported AI device → rule-only path.  
4. Kill app after capture before process → relaunch → processes.  
5. Share text into app → capture queued.  

### 10.3 Performance checks

Log timestamps:
- `t0` submit tapped  
- `t1` DB insert complete  
- `t2` UI popped  
- `t3` classification start/end  
- `t4` entity insert complete  

Targets in section 0.

---

## 11. Settings & monetization impact

**Current:** Free = rule-based; Premium = Cactus LLM.

**During this redesign:**
- Free users get **instant capture + rule-based background classify** (better than today).
- Premium may unlock platform AI enhancements **or** Cactus on devices without platform AI — product choice.
- Do not break billing code. If premium gate remains, gate only generative engines, never raw capture enqueue.

Recommended temporary policy:
- Everyone: enqueue + rule process.
- Premium: prefer platform native AI in router.
- Premium + no platform AI: optional Cactus.

Implement gating in router construction inside DI based on existing `isPremium` / settings flows.

---

## 12. Privacy checklist (non-negotiable)

- [ ] No network calls with user capture text for AI.
- [ ] Apple path uses on-device System Language Model only by default.
- [ ] Android path uses on-device AICore / ML Kit GenAI only.
- [ ] Speech recognition remains on-device flags already set in platform code.
- [ ] SQLCipher still always on.
- [ ] If a future Private Cloud Compute / cloud Gemini option is considered, it must be opt-in and out of scope here.

---

## 13. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| Foundation Models / AICore not on many devices | Router + rule fallback always |
| Platform JSON sometimes invalid | Reuse `JsonParser`; on failure rule fallback |
| WorkManager delays | Also process in-process immediately when app foregrounded |
| iOS background limits | Drain on launch; process immediately after enqueue in-app |
| KMP interop pain for Swift FM | Keep Swift helper tiny; only pass String in/out |
| Dual write bugs (capture + task) | Single ProcessCaptureUseCase owns transitions |
| User confusion ("where did my note go?") | Instant "Captured" + processing banner + appear in lists |
| Regression of review/edit | Keep task/note detail editors |

---

## 14. Explicit non-goals (do not do these in this project track)

- Building a chat UI.
- Cloud sync.
- Replacing SQLDelight.
- Re-adding Whisper/Cactus STT.
- Multi-task extraction from one long ramble (one capture → one note OR one task only).
- Full rewrite of Compose navigation.
- Changing encryption model.
- Desktop/JVM target.

---

## 15. Suggested PR sequence (for the implementing model)

1. **PR1:** EnqueueCapture + skip ParseReview + rule ProcessCapture + startup drain (Phase 1).  
2. **PR2:** Schema status fields + WorkManager + failure UI (Phase 2).  
3. **PR3:** CaptureIntelligence interface + router + rule adapter (Phase 3).  
4. **PR4:** Android PlatformCaptureIntelligence (Phase 4A).  
5. **PR5:** iOS Foundation Models bridge (Phase 4B).  
6. **PR6:** Heuristics, benchmarks, prompt polish (Phase 5).  

Each PR must: compile, pass `./gradlew :composeApp:testDebugUnitTest` (or project equivalent), and leave the app usable.

---

## 16. File creation checklist (expected new files)

```
composeApp/src/commonMain/kotlin/com/markduenas/localmind/
  ai/CaptureIntelligence.kt
  ai/RuleBasedCaptureIntelligence.kt
  ai/CaptureIntelligenceRouter.kt
  ai/CactusCaptureIntelligence.kt          # optional
  domain/usecase/EnqueueCaptureUseCase.kt
  domain/usecase/ProcessCaptureUseCase.kt
  domain/usecase/GetCaptureQueueStateUseCase.kt  # optional for banner
  platform/CaptureProcessor.kt             # expect

composeApp/src/androidMain/kotlin/com/markduenas/localmind/
  ai/PlatformCaptureIntelligence.android.kt
  platform/CaptureProcessor.android.kt
  worker/ProcessCaptureWorker.kt

composeApp/src/iosMain/kotlin/com/markduenas/localmind/
  ai/PlatformCaptureIntelligence.ios.kt
  platform/CaptureProcessor.ios.kt

iosApp/iosApp/
  CaptureClassifier.swift                  # Foundation Models helper
  # + bridge/bridge wiring as needed
```

Modify existing files listed in §2.2 and §5–6 rather than duplicating repositories.

---

## 17. Acceptance demo script (for human QA)

1. Fresh install, free user, airplane mode.  
2. Open Capture, type: `buy eggs tomorrow`. Submit.  
3. Confirm immediate return to list + "Captured".  
4. Within 1 second, task "buy eggs" (or similar title) appears with tomorrow due date.  
5. Mic: say `note to self, the cafe wifi password is orchid`.  
6. Confirm note appears with sensible title/body.  
7. Say `remind me to call Jordan Friday at 2pm`.  
8. Confirm scheduled task.  
9. On AI-capable device with premium (if gated), confirm `engine_used` is platform native in logs/DB.  
10. Force-quit after capture before item appears; reopen; item appears.

---

## 18. Summary for the implementing model (read this twice)

1. **Speed comes from UX architecture**, not from squeezing Cactus.  
2. Save raw capture first; classify in background; never block the user.  
3. Outputs are only: **note**, **task**, **task with schedule**.  
4. Use **Apple Foundation Models** and **Google on-device GenAI (Gemini Nano / ML Kit GenAI / AICore)** when available.  
5. **RuleBasedParser** is the universal backbone and current quality champion — keep it.  
6. Pluggable `CaptureIntelligence` + durable `captures` queue is the structural core of this plan.  
7. Ship Phase 1 before chasing platform SDKs. Instant capture alone is a major win.

---

## 19. Reference links (verify at implementation time)

- Apple Foundation Models: https://developer.apple.com/documentation/foundationmodels  
- Android Gemini Nano / on-device: https://developer.android.com/ai/gemini-nano  
- ML Kit GenAI: https://developers.google.com/ml-kit/genai  
- Existing LocalMind AI code: `composeApp/src/commonMain/kotlin/com/markduenas/localmind/ai/`  
- Benchmarks: `2026-03-20/benchmark-runs/` and `build/benchmark-reports/`  

---

*End of plan. Implement phase by phase. Prefer boring, reliable code over clever rewrites.*
