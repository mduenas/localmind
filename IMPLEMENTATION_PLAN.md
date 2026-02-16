# LocalMind Implementation Plan

**Version**: 1.0
**Date**: February 2026
**Target**: MVP Release

---

## Overview

This document provides a detailed, step-by-step implementation plan for LocalMind MVP. Each phase includes specific tasks, files to create/modify, dependencies, and acceptance criteria.

---

## Phase 1: Foundation Setup (Week 1)

### 1.1 Update Dependencies

**Goal**: Add all required dependencies to the project.

**Files to modify**:
- `gradle/libs.versions.toml`
- `composeApp/build.gradle.kts`
- `settings.gradle.kts`

**Dependencies to add**:
```toml
[versions]
koin = "4.0.0"
sqldelight = "2.0.2"
cactus = "1.2.0-beta"
kotlinx-datetime = "0.6.1"
kotlinx-serialization = "1.7.3"
turbine = "1.2.0"

[libraries]
# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

# SQLDelight
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

# Cactus AI
cactus = { module = "com.cactuscompute:cactus", version.ref = "cactus" }

# Kotlin extensions
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Testing
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

**Tasks**:
1. Add version catalog entries
2. Apply SQLDelight plugin to composeApp
3. Configure SQLDelight database name and package
4. Add Koin dependencies per source set
5. Add Cactus SDK dependency
6. Verify build succeeds: `./gradlew :composeApp:assemble`

**Acceptance criteria**:
- [ ] Clean build with no dependency conflicts
- [ ] SQLDelight generates database classes
- [ ] Koin initializes without errors

---

### 1.2 Project Structure Setup

**Goal**: Create the foundational package structure.

**Directories to create** (under `composeApp/src/commonMain/kotlin/com/markduenas/localmind/`):
```
di/
data/
  local/
  repository/
domain/
  model/
  usecase/
ai/
ui/
  theme/
  navigation/
  capture/
  tasks/
  review/
  settings/
  components/
util/
```

**Tasks**:
1. Create directory structure
2. Create placeholder files with package declarations
3. Remove demo files (Greeting.kt, existing App.kt content)

---

### 1.3 Koin Dependency Injection Setup

**Goal**: Configure Koin for multiplatform DI.

**Files to create**:
- `commonMain/.../di/AppModule.kt` - Shared module definitions
- `commonMain/.../di/KoinInit.kt` - Shared Koin initialization
- `androidMain/.../di/AndroidModule.kt` - Android-specific bindings
- `androidMain/.../LocalMindApplication.kt` - Android Application class
- `iosMain/.../di/IosModule.kt` - iOS-specific bindings

**AppModule.kt structure**:
```kotlin
val appModule = module {
    // Repositories
    singleOf(::TaskRepositoryImpl) { bind<TaskRepository>() }
    singleOf(::CaptureRepositoryImpl) { bind<CaptureRepository>() }

    // Use cases
    factoryOf(::ParseCaptureUseCase)
    factoryOf(::GetTodayTasksUseCase)
    factoryOf(::GetUpcomingTasksUseCase)

    // AI Services
    singleOf(::LLMService)
    singleOf(::STTService)
    singleOf(::TaskParser)

    // ViewModels
    viewModelOf(::CaptureViewModel)
    viewModelOf(::TaskListViewModel)
    viewModelOf(::ParseReviewViewModel)
    viewModelOf(::SettingsViewModel)
}
```

**Acceptance criteria**:
- [ ] Android app starts with Koin initialized
- [ ] iOS app starts with Koin initialized
- [ ] Test injection works in commonTest

---

### 1.4 SQLDelight Database Setup

**Goal**: Create database schema and driver factories.

**Files to create**:
- `commonMain/sqldelight/com/markduenas/localmind/LocalMindDb.sq`
- `commonMain/.../data/local/Database.kt` - expect declaration
- `androidMain/.../data/local/DatabaseDriverFactory.android.kt` - actual
- `iosMain/.../data/local/DatabaseDriverFactory.ios.kt` - actual

**LocalMindDb.sq**:
```sql
CREATE TABLE tasks (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    original_text TEXT NOT NULL,
    due_date INTEGER,
    due_time INTEGER,
    priority INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    completed_at INTEGER,
    parsing_confidence REAL
);

CREATE TABLE tags (
    id TEXT NOT NULL PRIMARY KEY,
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
    id TEXT NOT NULL PRIMARY KEY,
    raw_text TEXT NOT NULL,
    audio_path TEXT,
    created_at INTEGER NOT NULL,
    processed INTEGER NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created ON tasks(created_at DESC);

-- Queries
getTodayTasks:
SELECT * FROM tasks
WHERE (due_date = ? OR (due_date < ? AND status = 0)) AND status != 2
ORDER BY priority DESC, due_time ASC;

getUpcomingTasks:
SELECT * FROM tasks
WHERE due_date > ? AND due_date <= ? AND status != 2
ORDER BY due_date ASC, priority DESC;

getAllActiveTasks:
SELECT * FROM tasks WHERE status != 2 ORDER BY created_at DESC;

getTaskById:
SELECT * FROM tasks WHERE id = ?;

insertTask:
INSERT INTO tasks (id, title, original_text, due_date, due_time, priority, status, created_at, updated_at, parsing_confidence)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

updateTaskStatus:
UPDATE tasks SET status = ?, updated_at = ?, completed_at = ? WHERE id = ?;

deleteTask:
DELETE FROM tasks WHERE id = ?;

searchTasks:
SELECT * FROM tasks WHERE title LIKE ? OR original_text LIKE ? ORDER BY created_at DESC;

-- Tag queries
getAllTags:
SELECT * FROM tags ORDER BY name;

insertTag:
INSERT OR IGNORE INTO tags (id, name, color) VALUES (?, ?, ?);

getTagsForTask:
SELECT tags.* FROM tags
INNER JOIN task_tags ON tags.id = task_tags.tag_id
WHERE task_tags.task_id = ?;

addTagToTask:
INSERT OR IGNORE INTO task_tags (task_id, tag_id) VALUES (?, ?);

-- Capture queries
insertCapture:
INSERT INTO captures (id, raw_text, audio_path, created_at, processed) VALUES (?, ?, ?, ?, ?);

getUnprocessedCaptures:
SELECT * FROM captures WHERE processed = 0 ORDER BY created_at ASC;

markCaptureProcessed:
UPDATE captures SET processed = 1 WHERE id = ?;
```

**Acceptance criteria**:
- [ ] SQLDelight generates type-safe query classes
- [ ] Database creates successfully on both platforms
- [ ] CRUD operations work in integration test

---

## Phase 2: Domain & Data Layer (Week 2)

### 2.1 Domain Models

**Goal**: Create core domain models.

**Files to create**:
- `domain/model/Task.kt`
- `domain/model/Tag.kt`
- `domain/model/Capture.kt`
- `domain/model/Priority.kt`
- `domain/model/TaskStatus.kt`
- `domain/model/ParsedTask.kt`

**Task.kt**:
```kotlin
data class Task(
    val id: String,
    val title: String,
    val originalText: String,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val priority: Priority,
    val status: TaskStatus,
    val tags: List<Tag>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
    val parsingConfidence: Float?
)

enum class Priority { LOW, MEDIUM, HIGH }
enum class TaskStatus { PENDING, COMPLETED, ARCHIVED }
```

---

### 2.2 Repositories

**Goal**: Implement data access layer.

**Files to create**:
- `data/repository/TaskRepository.kt` - Interface
- `data/repository/TaskRepositoryImpl.kt` - Implementation
- `data/repository/CaptureRepository.kt` - Interface
- `data/repository/CaptureRepositoryImpl.kt` - Implementation
- `data/local/TaskDao.kt` - Query wrapper
- `data/local/Mappers.kt` - DB entity to domain model mappers

**TaskRepository interface**:
```kotlin
interface TaskRepository {
    fun getTodayTasks(): Flow<List<Task>>
    fun getUpcomingTasks(days: Int = 7): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    suspend fun createTask(task: Task)
    suspend fun updateTaskStatus(id: String, status: TaskStatus)
    suspend fun deleteTask(id: String)
    fun searchTasks(query: String): Flow<List<Task>>
}
```

**Acceptance criteria**:
- [ ] Repository methods return correct data types
- [ ] Flow emissions work reactively
- [ ] Integration tests pass

---

### 2.3 Use Cases

**Goal**: Implement business logic layer.

**Files to create**:
- `domain/usecase/ParseCaptureUseCase.kt`
- `domain/usecase/GetTodayTasksUseCase.kt`
- `domain/usecase/GetUpcomingTasksUseCase.kt`
- `domain/usecase/CreateTaskUseCase.kt`
- `domain/usecase/CompleteTaskUseCase.kt`
- `domain/usecase/ExportDataUseCase.kt`

**ParseCaptureUseCase.kt**:
```kotlin
class ParseCaptureUseCase(
    private val taskParser: TaskParser,
    private val ruleBasedParser: RuleBasedParser,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(rawText: String): ParseResult {
        return if (settingsRepository.isLLMEnabled()) {
            try {
                val parsed = taskParser.parse(rawText)
                ParseResult.Success(parsed)
            } catch (e: Exception) {
                // Fallback to rule-based
                val parsed = ruleBasedParser.parse(rawText)
                ParseResult.Fallback(parsed, reason = e.message)
            }
        } else {
            val parsed = ruleBasedParser.parse(rawText)
            ParseResult.Success(parsed)
        }
    }
}

sealed class ParseResult {
    data class Success(val task: ParsedTask) : ParseResult()
    data class Fallback(val task: ParsedTask, val reason: String?) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
```

---

## Phase 3: AI Integration (Week 3)

### 3.1 Cactus SDK Integration

**Goal**: Initialize and configure Cactus SDK.

**Files to create**:
- `ai/LLMService.kt` - LLM wrapper
- `ai/STTService.kt` - Speech-to-text wrapper
- `ai/ModelManager.kt` - Model download/management
- `ai/AIConfig.kt` - Configuration constants

**LLMService.kt**:
```kotlin
class LLMService(
    private val modelManager: ModelManager
) {
    private var cactusLM: CactusLM? = null

    suspend fun initialize() {
        if (cactusLM == null) {
            val modelPath = modelManager.getModelPath("qwen3-0.6")
            cactusLM = CactusLM().apply {
                initializeModel(modelPath)
            }
        }
    }

    suspend fun generateCompletion(prompt: String): String {
        val lm = cactusLM ?: throw IllegalStateException("LLM not initialized")
        return lm.generateCompletion(
            prompt = prompt,
            maxTokens = 256,
            temperature = 0.2f
        )
    }

    fun unload() {
        cactusLM?.unload()
        cactusLM = null
    }
}
```

**Android initialization** (`MainActivity.kt`):
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CactusContextInitializer.initialize(this)
    // ...
}
```

**Acceptance criteria**:
- [ ] Model downloads successfully
- [ ] LLM generates responses
- [ ] Memory is freed on unload

---

### 3.2 Task Parser

**Goal**: Implement LLM-based task parsing.

**Files to create**:
- `ai/TaskParser.kt` - Main parser orchestration
- `ai/Prompts.kt` - Prompt templates
- `ai/JsonParser.kt` - Response parsing

**TaskParser.kt**:
```kotlin
class TaskParser(
    private val llmService: LLMService
) {
    suspend fun parse(rawText: String): ParsedTask {
        val prompt = buildPrompt(rawText)
        val response = llmService.generateCompletion(prompt)
        return parseResponse(response, rawText)
    }

    private fun buildPrompt(input: String): String {
        return """
        |${SYSTEM_PROMPT}
        |
        |${FEW_SHOT_EXAMPLES}
        |
        |Input: "$input"
        |Output:
        """.trimMargin()
    }

    private fun parseResponse(json: String, originalText: String): ParsedTask {
        // Extract JSON from response, handle edge cases
        val extracted = extractJson(json)
        val parsed = Json.decodeFromString<TaskJson>(extracted)
        return parsed.toDomain(originalText)
    }
}
```

---

### 3.3 Rule-Based Parser (Fallback)

**Goal**: Implement regex-based task parsing for free tier.

**Files to create**:
- `ai/RuleBasedParser.kt`
- `ai/DatePatterns.kt`
- `ai/PriorityPatterns.kt`

**RuleBasedParser.kt**:
```kotlin
class RuleBasedParser {
    fun parse(rawText: String): ParsedTask {
        val dueDate = extractDate(rawText)
        val priority = extractPriority(rawText)
        val tags = extractTags(rawText)
        val title = extractTitle(rawText, tags)

        return ParsedTask(
            title = title,
            dueDate = dueDate,
            dueTime = null,
            priority = priority,
            tags = tags,
            originalText = rawText,
            confidence = 0.7f, // Rule-based has fixed confidence
            suggestedEdits = null
        )
    }

    private fun extractDate(text: String): LocalDate? {
        // Pattern matching for: tomorrow, next Monday, in 3 days, Mar 15, etc.
        DATE_PATTERNS.forEach { (pattern, resolver) ->
            pattern.find(text)?.let { match ->
                return resolver(match)
            }
        }
        return null
    }

    private fun extractPriority(text: String): Priority {
        val upper = text.uppercase()
        return when {
            URGENT_PATTERNS.any { it in upper } -> Priority.HIGH
            LOW_PATTERNS.any { it in upper } -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }

    private fun extractTags(text: String): List<String> {
        return HASHTAG_PATTERN.findAll(text)
            .map { it.groupValues[1] }
            .toList()
    }
}
```

---

### 3.4 Speech-to-Text Integration

**Goal**: Implement voice capture with transcription.

**Files to create**:
- `ai/STTService.kt` - Speech transcription wrapper
- `ui/capture/VoiceRecorder.kt` - Audio capture logic
- `ui/capture/AudioWaveform.kt` - Waveform visualization

**STTService.kt**:
```kotlin
class STTService(
    private val modelManager: ModelManager
) {
    private var cactusSTT: CactusSTT? = null

    suspend fun initialize() {
        if (cactusSTT == null) {
            val modelPath = modelManager.getModelPath("whisper-tiny")
            cactusSTT = CactusSTT().apply {
                initializeModel(modelPath)
            }
        }
    }

    suspend fun transcribe(audioPath: String): String {
        val stt = cactusSTT ?: throw IllegalStateException("STT not initialized")
        return stt.transcribe(audioPath)
    }

    fun transcribeStreaming(
        audioPath: String,
        onPartialResult: (String) -> Unit,
        onComplete: (String) -> Unit
    ) {
        // Streaming transcription with callbacks
    }
}
```

**Acceptance criteria**:
- [ ] Voice recording works on both platforms
- [ ] Transcription completes within performance targets
- [ ] Waveform visualization updates in real-time

---

## Phase 4: UI Implementation (Week 4-5)

### 4.1 Theme & Design System

**Goal**: Implement Material 3 theming.

**Files to create**:
- `ui/theme/Theme.kt`
- `ui/theme/Colors.kt`
- `ui/theme/Typography.kt`
- `ui/theme/Shapes.kt`

**Theme.kt**:
```kotlin
@Composable
fun LocalMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LocalMindTypography,
        shapes = LocalMindShapes,
        content = content
    )
}
```

---

### 4.2 Navigation Setup

**Goal**: Implement app navigation.

**Files to create**:
- `ui/navigation/NavGraph.kt`
- `ui/navigation/Screen.kt` - Route definitions
- `ui/navigation/BottomNavBar.kt`

**Screen.kt**:
```kotlin
sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Upcoming : Screen("upcoming")
    object AllTasks : Screen("all")
    object Capture : Screen("capture")
    object ParseReview : Screen("review/{captureId}") {
        fun createRoute(captureId: String) = "review/$captureId"
    }
    object Settings : Screen("settings")
    object Search : Screen("search")
}
```

---

### 4.3 Capture Screen

**Goal**: Implement quick capture UI.

**Files to create**:
- `ui/capture/CaptureScreen.kt`
- `ui/capture/CaptureViewModel.kt`
- `ui/capture/TextCaptureCard.kt`
- `ui/capture/VoiceCaptureCard.kt`
- `ui/components/FloatingCaptureButton.kt`

**CaptureViewModel.kt**:
```kotlin
class CaptureViewModel(
    private val captureRepository: CaptureRepository,
    private val sttService: STTService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun onTextInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun startVoiceCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecording = true) }
            // Start recording...
        }
    }

    fun stopVoiceCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecording = false, isTranscribing = true) }
            val transcription = sttService.transcribe(audioPath)
            _uiState.update {
                it.copy(
                    isTranscribing = false,
                    inputText = transcription
                )
            }
        }
    }

    fun submitCapture() {
        viewModelScope.launch {
            val capture = Capture(
                id = UUID.randomUUID().toString(),
                rawText = _uiState.value.inputText,
                createdAt = Clock.System.now()
            )
            captureRepository.save(capture)
            // Navigate to parse review
        }
    }
}

data class CaptureUiState(
    val inputText: String = "",
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val audioAmplitude: Float = 0f
)
```

---

### 4.4 Parse Review Screen

**Goal**: Implement task review/edit UI.

**Files to create**:
- `ui/review/ParseReviewScreen.kt`
- `ui/review/ParseReviewViewModel.kt`
- `ui/review/TaskPreviewCard.kt`
- `ui/review/FieldEditor.kt`

**ParseReviewScreen.kt** key components:
- Original text display (read-only)
- Parsed fields with inline editing
- Confidence indicator
- "Re-parse" button
- "Save" / "Save as raw" actions

---

### 4.5 Task List Screens

**Goal**: Implement Today/Upcoming/All task views.

**Files to create**:
- `ui/tasks/TodayScreen.kt`
- `ui/tasks/UpcomingScreen.kt`
- `ui/tasks/AllTasksScreen.kt`
- `ui/tasks/TaskListViewModel.kt`
- `ui/tasks/TaskCard.kt`
- `ui/tasks/TaskSection.kt` - Grouped by date/priority
- `ui/tasks/EmptyState.kt`

**TaskCard.kt**:
```kotlin
@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = { onComplete() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.status == TaskStatus.COMPLETED)
                        TextDecoration.LineThrough else null
                )
                task.dueDate?.let { date ->
                    Text(
                        text = formatDueDate(date, task.dueTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = getDueDateColor(date)
                    )
                }
                if (task.tags.isNotEmpty()) {
                    FlowRow {
                        task.tags.forEach { tag ->
                            TagChip(tag = tag)
                        }
                    }
                }
            }
            PriorityIndicator(priority = task.priority)
        }
    }
}
```

---

### 4.6 Settings Screen

**Goal**: Implement settings UI.

**Files to create**:
- `ui/settings/SettingsScreen.kt`
- `ui/settings/SettingsViewModel.kt`
- `ui/settings/ModelManagementSection.kt`
- `ui/settings/SecuritySection.kt`
- `ui/settings/NotificationSection.kt`
- `ui/settings/ExportSection.kt`
- `data/repository/SettingsRepository.kt`

---

## Phase 5: Platform Integration (Week 6)

### 5.1 Android Notifications

**Goal**: Implement daily summary notifications.

**Files to create**:
- `androidMain/.../notification/NotificationHelper.kt`
- `androidMain/.../notification/SummaryWorker.kt`
- `androidMain/.../notification/NotificationChannels.kt`

**SummaryWorker.kt**:
```kotlin
class SummaryWorker(
    context: Context,
    params: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tasks = taskRepository.getTodayTasks().first()
        val overdue = tasks.filter { it.dueDate?.isBefore(today) == true }

        showNotification(
            title = "Today's Tasks",
            body = buildSummaryText(tasks, overdue)
        )

        return Result.success()
    }
}
```

---

### 5.2 iOS Notifications

**Goal**: Implement iOS notification support.

**Files to create**:
- `iosMain/.../notification/NotificationHelper.ios.kt`

**Implementation**:
- Use UNUserNotificationCenter
- Request notification permissions
- Schedule local notifications

---

### 5.3 Android Widget

**Goal**: Implement home screen widget for quick capture.

**Files to create**:
- `androidMain/.../widget/CaptureWidget.kt`
- `androidMain/.../widget/CaptureWidgetReceiver.kt`
- `androidMain/res/xml/capture_widget_info.xml`
- `androidMain/res/layout/widget_capture.xml`

---

### 5.4 Share Sheet Integration

**Goal**: Receive shared text from other apps.

**Android** (`AndroidManifest.xml`):
```xml
<activity android:name=".ShareReceiverActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

---

## Phase 6: Polish & Testing (Week 7-8)

### 6.1 Error Handling

**Goal**: Implement comprehensive error handling.

**Tasks**:
- Add try-catch to all AI operations
- Implement retry logic for model operations
- Add user-friendly error messages
- Implement offline detection and handling

---

### 6.2 Performance Optimization

**Goal**: Meet performance targets.

**Tasks**:
- Profile cold start time
- Optimize Compose recomposition (use `remember`, `derivedStateOf`)
- Implement lazy loading for task lists
- Add loading states and skeletons
- Profile memory during LLM inference

---

### 6.3 Unit Tests

**Goal**: Test coverage for critical paths.

**Files to create**:
- `commonTest/.../domain/usecase/ParseCaptureUseCaseTest.kt`
- `commonTest/.../ai/RuleBasedParserTest.kt`
- `commonTest/.../ai/TaskParserTest.kt`
- `commonTest/.../data/repository/TaskRepositoryTest.kt`

**RuleBasedParserTest.kt**:
```kotlin
class RuleBasedParserTest {
    private val parser = RuleBasedParser()

    @Test
    fun `parses tomorrow correctly`() {
        val result = parser.parse("call mom tomorrow")
        assertEquals(Clock.System.now().plus(1.days).date, result.dueDate)
    }

    @Test
    fun `extracts hashtags as tags`() {
        val result = parser.parse("fix bug #work #urgent")
        assertEquals(listOf("work", "urgent"), result.tags)
    }

    @Test
    fun `detects high priority keywords`() {
        val result = parser.parse("URGENT fix server")
        assertEquals(Priority.HIGH, result.priority)
    }
}
```

---

### 6.4 Integration Tests

**Goal**: End-to-end flow testing.

**Tests to implement**:
- Capture → Parse → Save → Display flow
- Voice capture → Transcription → Parse flow
- Task completion flow
- Export functionality

---

### 6.5 Accessibility Audit

**Goal**: Ensure accessibility compliance.

**Tasks**:
- Add contentDescription to all interactive elements
- Test with TalkBack / VoiceOver
- Verify touch target sizes (48dp minimum)
- Test with large font sizes
- Verify color contrast ratios

---

### 6.6 Beta Build Preparation

**Goal**: Prepare for beta testing.

**Tasks**:
- Create release signing configuration
- Set up app icons and splash screen
- Write app store description
- Create screenshots
- Configure ProGuard/R8 rules
- Test release build thoroughly

---

## Dependency Graph

```
Phase 1 ─────────────────┐
  └─ Dependencies        │
  └─ Structure          │
  └─ Koin               │
  └─ SQLDelight ────────┼──► Phase 2 ────────────────┐
                        │      └─ Domain Models      │
                        │      └─ Repositories       │
                        │      └─ Use Cases ─────────┼──► Phase 3
                        │                            │      └─ Cactus SDK
                        │                            │      └─ Task Parser
                        │                            │      └─ Rule Parser
                        │                            │      └─ STT ──────────┐
                        │                            │                       │
                        │                            └──────────────────────┼──► Phase 4
                        │                                                   │      └─ Theme
                        │                                                   │      └─ Navigation
                        │                                                   │      └─ Capture UI
                        │                                                   │      └─ Review UI
                        │                                                   │      └─ Task Lists
                        │                                                   │      └─ Settings
                        │                                                   │
                        └───────────────────────────────────────────────────┼──► Phase 5
                                                                            │      └─ Notifications
                                                                            │      └─ Widget
                                                                            │      └─ Share Sheet
                                                                            │
                                                                            └──► Phase 6
                                                                                   └─ Error Handling
                                                                                   └─ Performance
                                                                                   └─ Testing
                                                                                   └─ Beta Prep
```

---

## Risk Checkpoints

| Checkpoint | Week | Go/No-Go Criteria |
|------------|------|-------------------|
| Dependencies verified | 1 | All libraries integrate without conflicts |
| Database working | 1 | CRUD operations work on both platforms |
| LLM inference working | 3 | Model loads and generates output on test device |
| Voice capture working | 3 | Recording + transcription works on both platforms |
| Core flow complete | 5 | Capture → Parse → Save → Display works end-to-end |
| Performance acceptable | 7 | Meets targets on Pixel 6 / iPhone 12 |
| Beta ready | 8 | No P0 bugs, release build stable |

---

## File Checklist

### Phase 1 (Foundation)
- [ ] `gradle/libs.versions.toml` - Updated
- [ ] `composeApp/build.gradle.kts` - Updated
- [ ] `settings.gradle.kts` - Updated
- [ ] `commonMain/.../di/AppModule.kt`
- [ ] `commonMain/.../di/KoinInit.kt`
- [ ] `androidMain/.../di/AndroidModule.kt`
- [ ] `androidMain/.../LocalMindApplication.kt`
- [ ] `iosMain/.../di/IosModule.kt`
- [ ] `commonMain/sqldelight/.../LocalMindDb.sq`
- [ ] `commonMain/.../data/local/Database.kt`
- [ ] `androidMain/.../data/local/DatabaseDriverFactory.android.kt`
- [ ] `iosMain/.../data/local/DatabaseDriverFactory.ios.kt`

### Phase 2 (Domain/Data)
- [ ] `domain/model/Task.kt`
- [ ] `domain/model/Tag.kt`
- [ ] `domain/model/Capture.kt`
- [ ] `domain/model/Priority.kt`
- [ ] `domain/model/TaskStatus.kt`
- [ ] `domain/model/ParsedTask.kt`
- [ ] `data/repository/TaskRepository.kt`
- [ ] `data/repository/TaskRepositoryImpl.kt`
- [ ] `data/repository/CaptureRepository.kt`
- [ ] `data/repository/CaptureRepositoryImpl.kt`
- [ ] `data/local/TaskDao.kt`
- [ ] `data/local/Mappers.kt`
- [ ] `domain/usecase/ParseCaptureUseCase.kt`
- [ ] `domain/usecase/GetTodayTasksUseCase.kt`
- [ ] `domain/usecase/GetUpcomingTasksUseCase.kt`
- [ ] `domain/usecase/CreateTaskUseCase.kt`
- [ ] `domain/usecase/CompleteTaskUseCase.kt`
- [ ] `domain/usecase/ExportDataUseCase.kt`

### Phase 3 (AI)
- [ ] `ai/LLMService.kt`
- [ ] `ai/STTService.kt`
- [ ] `ai/ModelManager.kt`
- [ ] `ai/AIConfig.kt`
- [ ] `ai/TaskParser.kt`
- [ ] `ai/Prompts.kt`
- [ ] `ai/JsonParser.kt`
- [ ] `ai/RuleBasedParser.kt`
- [ ] `ai/DatePatterns.kt`
- [ ] `ai/PriorityPatterns.kt`
- [ ] `ui/capture/VoiceRecorder.kt`
- [ ] `ui/capture/AudioWaveform.kt`

### Phase 4 (UI)
- [ ] `ui/theme/Theme.kt`
- [ ] `ui/theme/Colors.kt`
- [ ] `ui/theme/Typography.kt`
- [ ] `ui/theme/Shapes.kt`
- [ ] `ui/navigation/NavGraph.kt`
- [ ] `ui/navigation/Screen.kt`
- [ ] `ui/navigation/BottomNavBar.kt`
- [ ] `ui/capture/CaptureScreen.kt`
- [ ] `ui/capture/CaptureViewModel.kt`
- [ ] `ui/capture/TextCaptureCard.kt`
- [ ] `ui/capture/VoiceCaptureCard.kt`
- [ ] `ui/components/FloatingCaptureButton.kt`
- [ ] `ui/review/ParseReviewScreen.kt`
- [ ] `ui/review/ParseReviewViewModel.kt`
- [ ] `ui/review/TaskPreviewCard.kt`
- [ ] `ui/review/FieldEditor.kt`
- [ ] `ui/tasks/TodayScreen.kt`
- [ ] `ui/tasks/UpcomingScreen.kt`
- [ ] `ui/tasks/AllTasksScreen.kt`
- [ ] `ui/tasks/TaskListViewModel.kt`
- [ ] `ui/tasks/TaskCard.kt`
- [ ] `ui/tasks/TaskSection.kt`
- [ ] `ui/tasks/EmptyState.kt`
- [ ] `ui/settings/SettingsScreen.kt`
- [ ] `ui/settings/SettingsViewModel.kt`
- [ ] `ui/settings/ModelManagementSection.kt`
- [ ] `ui/settings/SecuritySection.kt`
- [ ] `ui/settings/NotificationSection.kt`
- [ ] `ui/settings/ExportSection.kt`
- [ ] `data/repository/SettingsRepository.kt`

### Phase 5 (Platform)
- [ ] `androidMain/.../notification/NotificationHelper.kt`
- [ ] `androidMain/.../notification/SummaryWorker.kt`
- [ ] `androidMain/.../notification/NotificationChannels.kt`
- [ ] `iosMain/.../notification/NotificationHelper.ios.kt`
- [ ] `androidMain/.../widget/CaptureWidget.kt`
- [ ] `androidMain/.../widget/CaptureWidgetReceiver.kt`
- [ ] `androidMain/res/xml/capture_widget_info.xml`
- [ ] `androidMain/res/layout/widget_capture.xml`
- [ ] `androidMain/.../ShareReceiverActivity.kt`

### Phase 6 (Testing)
- [ ] `commonTest/.../domain/usecase/ParseCaptureUseCaseTest.kt`
- [ ] `commonTest/.../ai/RuleBasedParserTest.kt`
- [ ] `commonTest/.../ai/TaskParserTest.kt`
- [ ] `commonTest/.../data/repository/TaskRepositoryTest.kt`
