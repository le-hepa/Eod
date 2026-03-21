package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.repository.TaskTopRepository
import com.gomgom.eod.feature.task.repository.TaskTopRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskVesselAddViewModel(
    private val repository: TaskTopRepository = TaskTopRepositoryProvider.repository
) : ViewModel() {

    private val _vesselName = MutableStateFlow("")
    val vesselName: StateFlow<String> = _vesselName.asStateFlow()

    fun onVesselNameChange(value: String) {
        _vesselName.value = value
    }

    fun save(selectedPresetName: String): Long? {
        val trimmed = _vesselName.value.trim()
        if (trimmed.isEmpty()) return null
        if (selectedPresetName.trim().isEmpty()) return null

        val newId = repository.addVessel(trimmed, selectedPresetName)
        if (newId != null) {
            _vesselName.value = ""
        }
        return newId
    }
}
