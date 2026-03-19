package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

object TaskPresetStateStore {
    private val nextId = AtomicLong(1L)

    private val _presetGroups = kotlinx.coroutines.flow.MutableStateFlow<List<TaskPresetGroupItem>>(emptyList())
    val presetGroups = _presetGroups

    fun addPreset(name: String): Long? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null

        val createdId = nextId.getAndIncrement()

        _presetGroups.update { current ->
            val shouldEnable = current.isEmpty()
            current + TaskPresetGroupItem(
                id = createdId,
                name = trimmed,
                enabled = shouldEnable
            )
        }

        return createdId
    }

    fun setPresetEnabled(presetId: Long, enabled: Boolean) {
        _presetGroups.update { current ->
            if (enabled) {
                current.map { item ->
                    item.copy(enabled = item.id == presetId)
                }
            } else {
                current.map { item ->
                    if (item.id == presetId) item.copy(enabled = false) else item
                }
            }
        }
    }
}

class TaskPresetViewModel : ViewModel() {

    val uiState: StateFlow<TaskTopUiState> =
        TaskPresetStateStore.presetGroups
            .map { TaskTopUiState(presetGroups = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TaskTopUiState()
            )

    fun addPreset(name: String): Long? {
        return TaskPresetStateStore.addPreset(name)
    }

    fun onPresetToggle(presetId: Long, enabled: Boolean) {
        TaskPresetStateStore.setPresetEnabled(presetId, enabled)
    }
}