package com.markduenas.localmind.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.billing.BillingProduct
import com.markduenas.localmind.billing.ProductIds
import com.markduenas.localmind.billing.ProductType
import com.markduenas.localmind.domain.model.Note
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.model.Tag
import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import com.markduenas.localmind.ui.capture.TextCaptureCard
import com.markduenas.localmind.ui.capture.VoiceCaptureCard
import com.markduenas.localmind.ui.notes.NoteCard
import com.markduenas.localmind.ui.paywall.PaywallContent
import com.markduenas.localmind.ui.settings.ExportSection
import com.markduenas.localmind.ui.settings.ModelManagementSection
import com.markduenas.localmind.ai.ModelDownloadState
import com.markduenas.localmind.ui.settings.NotificationSection
import com.markduenas.localmind.ui.settings.PremiumStatusSection
import com.markduenas.localmind.ui.tasks.TaskCard
import com.markduenas.localmind.ui.theme.LocalMindTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class StoreScreenshots {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6_PRO,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    // Fixed timestamp for reproducible screenshots (2026-03-02T12:00:00Z)
    private val now: Instant = Instant.fromEpochSeconds(1772452800L)

    // ── Sample Data ──

    private val sampleTasks = listOf(
        Task(
            id = "1",
            title = "Call dentist",
            originalText = "call dentist tomorrow at 2pm",
            dueDate = LocalDate(2026, 3, 3),
            dueTime = LocalTime(14, 0),
            priority = Priority.MEDIUM,
            status = TaskStatus.PENDING,
            tags = listOf(Tag("t1", "health", null)),
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            parsingConfidence = 0.95f,
        ),
        Task(
            id = "2",
            title = "Submit quarterly report",
            originalText = "submit quarterly report by friday high priority",
            dueDate = LocalDate(2026, 3, 6),
            dueTime = null,
            priority = Priority.HIGH,
            status = TaskStatus.PENDING,
            tags = listOf(Tag("t2", "work", null)),
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            parsingConfidence = 0.92f,
        ),
        Task(
            id = "3",
            title = "Buy groceries",
            originalText = "buy groceries tomorrow #errands",
            dueDate = LocalDate(2026, 3, 3),
            dueTime = null,
            priority = Priority.MEDIUM,
            status = TaskStatus.PENDING,
            tags = listOf(Tag("t3", "errands", null)),
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            parsingConfidence = 0.88f,
        ),
        Task(
            id = "4",
            title = "Review pull request",
            originalText = "review pull request",
            dueDate = LocalDate(2026, 3, 2),
            dueTime = null,
            priority = Priority.LOW,
            status = TaskStatus.COMPLETED,
            tags = listOf(Tag("t4", "dev", null)),
            createdAt = now,
            updatedAt = now,
            completedAt = now,
            parsingConfidence = 0.85f,
        ),
    )

    private val sampleNotes = listOf(
        Note(
            id = "n1",
            title = "Great pasta at Italian place",
            body = "Great pasta at the Italian place on 5th street. Try the carbonara next time.",
            originalText = "great pasta at the italian place on 5th street",
            tags = listOf(Tag("tn1", "food", null)),
            createdAt = now,
            updatedAt = now,
            parsingConfidence = 0.90f,
        ),
        Note(
            id = "n2",
            title = "Neural networks and creativity",
            body = "Interesting idea about neural networks and creativity — could generative models actually be creative or just interpolating?",
            originalText = "interesting idea about neural networks and creativity",
            tags = listOf(Tag("tn2", "ideas", null)),
            createdAt = now,
            updatedAt = now,
            parsingConfidence = 0.88f,
        ),
        Note(
            id = "n3",
            title = "Book recommendation: Deep Work",
            body = "Sarah recommended Deep Work by Cal Newport. About focused productivity without distractions.",
            originalText = "sarah recommended deep work by cal newport",
            tags = listOf(Tag("tn3", "books", null)),
            createdAt = now,
            updatedAt = now,
            parsingConfidence = 0.85f,
        ),
    )

    private val sampleProducts = listOf(
        BillingProduct(
            id = ProductIds.PREMIUM_LIFETIME,
            title = "LocalMind Premium (Lifetime)",
            description = "Unlock all features with a one-time purchase",
            formattedPrice = "$24.99",
            priceAmountMicros = 24_990_000L,
            priceCurrencyCode = "USD",
            productType = ProductType.ONE_TIME,
        ),
        BillingProduct(
            id = ProductIds.PREMIUM_MONTHLY,
            title = "LocalMind Premium",
            description = "Monthly subscription for all features",
            formattedPrice = "$3.99",
            priceAmountMicros = 3_990_000L,
            priceCurrencyCode = "USD",
            productType = ProductType.SUBSCRIPTION,
        ),
    )

    // ── Screenshots ──

    @Test
    fun todayView() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                ) {
                    items(sampleTasks) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = {},
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun textCapture() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                CaptureScreenPreview(selectedTab = 1)
            }
        }
    }

    @Test
    fun voiceCapture() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                CaptureScreenPreview(selectedTab = 0)
            }
        }
    }

    @Test
    fun parseReview() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                ParseReviewPreview()
            }
        }
    }

    @Test
    fun settingsScreen() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                SettingsPreview(isPremium = false)
            }
        }
    }

    @Test
    fun settingsPremium() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                SettingsPreview(isPremium = true)
            }
        }
    }

    @Test
    fun paywallSheet() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                PaywallContent(
                    products = sampleProducts,
                    purchaseInProgress = false,
                    restoreInProgress = false,
                    onPurchase = {},
                    onRestore = {},
                    onDismiss = {},
                )
            }
        }
    }

    @Test
    fun notesView() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                ) {
                    items(sampleNotes) { note ->
                        NoteCard(
                            note = note,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun upcomingView() {
        paparazzi.snapshot {
            LocalMindTheme(darkTheme = false) {
                val grouped = sampleTasks
                    .filter { it.status == TaskStatus.PENDING }
                    .groupBy { it.dueDate }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                ) {
                    grouped.forEach { (date, tasks) ->
                        @Suppress("DEPRECATION")
                        val sectionTitle = date?.let {
                            "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}"
                        } ?: "No date"
                        item {
                            Text(
                                text = sectionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(tasks) { task ->
                            TaskCard(
                                task = task,
                                onToggleComplete = {},
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Preview Wrappers ──

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CaptureScreenPreview(selectedTab: Int) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Capture") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = {}, text = { Text("Voice") })
                    Tab(selected = selectedTab == 1, onClick = {}, text = { Text("Text") })
                }
                Spacer(Modifier.height(16.dp))
                when (selectedTab) {
                    0 -> VoiceCaptureCard(isRecording = false, onToggleRecording = {})
                    1 -> TextCaptureCard(
                        text = "Buy groceries tomorrow at 3pm #errands",
                        onTextChanged = {},
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ParseReviewPreview() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Review Task") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Original",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "\"buy groceries tomorrow at 3pm #errands\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Spacer(Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        onClick = {},
                        selected = true,
                    ) { Text("Task") }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        onClick = {},
                        selected = false,
                    ) { Text("Note") }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = "Buy groceries",
                    onValueChange = {},
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "3/3/2026",
                    onValueChange = {},
                    label = { Text("Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "3:00 PM",
                    onValueChange = {},
                    label = { Text("Due Time") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "Medium",
                    onValueChange = {},
                    label = { Text("Priority") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }

    @Composable
    private fun SettingsPreview(isPremium: Boolean) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PremiumStatusSection(
                isPremium = isPremium,
                onUpgrade = {},
            )

            HorizontalDivider()

            // LLM toggle
            Column {
                Text(
                    text = "AI Parsing",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On-Device LLM", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Use AI model for smarter task parsing (requires model download)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = isPremium, onCheckedChange = {})
                }
            }

            HorizontalDivider()

            ModelManagementSection(
                downloadedModels = if (isPremium) listOf("qwen3-0.6") else emptyList(),
                availableModels = listOf("qwen3-0.6", "gemma3-270m"),
                selectedLlmModel = "qwen3-0.6",
                downloadState = ModelDownloadState.Idle,
                onDownloadModel = {},
                onDeleteModel = {},
                onSelectModel = {},
                onRetryDownload = {},
                onDismissError = {},
            )

            HorizontalDivider()

            NotificationSection(
                notificationsEnabled = true,
                onNotificationsChanged = {},
            )

            HorizontalDivider()

            ExportSection(
                onExportTasks = {},
                isPremium = isPremium,
                onUpgradeRequired = {},
            )
        }
    }
}
