package com.markduenas.localmind.data.repository

import com.markduenas.localmind.platform.PlatformSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val settings: PlatformSettings) {

    private val _llmEnabled = MutableStateFlow(settings.getBoolean(KEY_LLM_ENABLED, false))
    val llmEnabled: StateFlow<Boolean> = _llmEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(settings.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _selectedLlmModel = MutableStateFlow(settings.getString(KEY_SELECTED_LLM_MODEL, ""))
    val selectedLlmModel: StateFlow<String> = _selectedLlmModel.asStateFlow()

    fun setLlmEnabled(enabled: Boolean) {
        _llmEnabled.value = enabled
        settings.putBoolean(KEY_LLM_ENABLED, enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        settings.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    fun setSelectedLlmModel(slug: String) {
        _selectedLlmModel.value = slug
        settings.putString(KEY_SELECTED_LLM_MODEL, slug)
    }

    fun getString(key: String, default: String = ""): String = settings.getString(key, default)

    fun putString(key: String, value: String) {
        settings.putString(key, value)
    }

    companion object {
        private const val KEY_LLM_ENABLED = "llm_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_SELECTED_LLM_MODEL = "selected_llm_model"
    }
}
