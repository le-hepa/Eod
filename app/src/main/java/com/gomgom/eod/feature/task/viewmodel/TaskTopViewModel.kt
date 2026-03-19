package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.repository.TaskTopRepository
import com.gomgom.eod.feature.task.repository.TaskTopRepositoryProvider
import kotlinx.coroutines.flow.StateFlow

class TaskTopViewModel(
    private val repository: TaskTopRepository = TaskTopRepositoryProvider.repository
) : ViewModel() {

    val uiState: StateFlow<TaskTopUiState> = repository.uiState

    fun onAlarmToggle(checked: Boolean) {
        repository.setAlarmEnabled(checked)
    }

    fun onVesselToggle(
        vesselId: Long,
        checked: Boolean
    ) {
        repository.setVesselEnabled(vesselId, checked)
    }

    fun addVessel(
        name: String,
        presetName: String
    ) {
        repository.addVessel(name, presetName)
    }

    fun updateVesselName(
        vesselId: Long,
        name: String
    ) {
        repository.updateVesselName(vesselId, name)
    }

    fun deleteVessel(vesselId: Long) {
        repository.deleteVessel(vesselId)
    }

    fun addPresetGroup(name: String) {
        repository.addPresetGroup(name)
    }

    fun setPresetGroupEnabled(
        presetId: Long,
        enabled: Boolean
    ) {
        repository.setPresetGroupEnabled(presetId, enabled)
    }

    fun savePresetWork(
        presetId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        repository.savePresetWork(
            presetId = presetId,
            workName = workName,
            reference = reference,
            cycle = cycle
        )
    }

    fun updatePresetWork(
        presetId: Long,
        workId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        repository.updatePresetWork(
            presetId = presetId,
            workId = workId,
            workName = workName,
            reference = reference,
            cycle = cycle
        )
    }

    fun deletePresetWork(
        presetId: Long,
        workId: Long
    ) {
        repository.deletePresetWork(
            presetId = presetId,
            workId = workId
        )
    }

    fun deletePresetGroup(presetId: Long) {
        repository.deletePresetGroup(presetId)
    }

    fun clearAll() {
        repository.clearAll()
    }
}