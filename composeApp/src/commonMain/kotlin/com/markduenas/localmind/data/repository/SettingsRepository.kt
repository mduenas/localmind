package com.markduenas.localmind.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository {

    private val store = mutableMapOf<String, String>()

    private val _llmEnabled = MutableStateFlow(false)
    val llmEnabled: StateFlow<Boolean> = _llmEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _selectedLlmModel = MutableStateFlow("")
    val selectedLlmModel: StateFlow<String> = _selectedLlmModel.asStateFlow()

    fun setLlmEnabled(enabled: Boolean) {
        _llmEnabled.value = enabled
        store["llm_enabled"] = enabled.toString()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        store["notifications_enabled"] = enabled.toString()
    }

    fun setSelectedLlmModel(slug: String) {
        _selectedLlmModel.value = slug
        store["selected_llm_model"] = slug
    }

    fun getString(key: String, default: String = ""): String = store[key] ?: default

    fun putString(key: String, value: String) {
        store[key] = value
    }
}
