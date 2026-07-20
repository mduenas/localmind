package com.markduenas.localmind.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.platform.SpeechActivityFallbackEffect
import com.markduenas.localmind.platform.SpeechPermissionEffect
import com.markduenas.localmind.platform.SpeechRecognitionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val CAPTURED_CONFIRMATION_MILLIS = 350L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onCaptured: () -> Unit,
    onBack: () -> Unit,
    viewModel: CaptureViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(if (state.defaultToTextCapture) 1 else 0) }
    var justCaptured by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val speechService = koinInject<SpeechRecognitionService>()
    val uriHandler = LocalUriHandler.current

    SpeechPermissionEffect(
        shouldRequest = state.needsSpeechPermission,
        onResult = viewModel::onSpeechPermissionResult,
    )

    SpeechActivityFallbackEffect(speechService)

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = state.errorActionLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                state.errorActionUrl?.let { uriHandler.openUri(it) }
            }
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.captured.collect {
            justCaptured = true
            delay(CAPTURED_CONFIRMATION_MILLIS)
            onCaptured()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (justCaptured) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Captured ✓", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                @OptIn(ExperimentalMaterial3Api::class)
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Voice") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Text") },
                    )
                }

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> VoiceCaptureCard(
                        isRecording = state.isRecording,
                        onToggleRecording = viewModel::toggleRecording,
                    )
                    1 -> TextCaptureCard(
                        text = state.inputText,
                        onTextChanged = viewModel::onTextChanged,
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.submit() },
                        modifier = Modifier.weight(1f),
                        enabled = state.inputText.isNotBlank(),
                    ) {
                        Text("Capture")
                    }
                }
            }
        }
    }
}
