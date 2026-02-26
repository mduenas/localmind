package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable

@Composable
expect fun SpeechPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
)
