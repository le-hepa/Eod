package com.gomgom.eod.feature.task.repository

import com.gomgom.eod.feature.task.viewmodel.TaskTopUiState
import kotlinx.coroutines.flow.StateFlow

interface TaskTopRepository {
    val uiState: StateFlow<TaskTopUiState>

    fun setAlarmEnabled(enabled: Boolean)

    fun setVesselEnabled(
        vesselId: Long,
        enabled: Boolean
    )

    fun addVessel(
        name: String,
        presetName: String
    ): Long?

    fun updateVesselName(
        vesselId: Long,
        name: String
    )

    fun deleteVessel(vesselId: Long)

    fun addPresetGroup(name: String)

    fun setPresetGroupEnabled(
        presetId: Long,
        enabled: Boolean
    )

    fun savePresetWork(
        presetId: Long,
        workName: String,
        reference: String,
        cycle: String
    )

    fun updatePresetWork(
        presetId: Long,
        workId: Long,
        workName: String,
        reference: String,
        cycle: String
    )

    fun deletePresetWork(
        presetId: Long,
        workId: Long
    )

    fun deletePresetGroup(presetId: Long)

    fun clearAll()
}