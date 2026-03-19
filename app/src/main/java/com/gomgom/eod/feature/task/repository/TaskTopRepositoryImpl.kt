package com.gomgom.eod.feature.task.repository

import com.gomgom.eod.feature.task.dao.TaskTopDao
import com.gomgom.eod.feature.task.dao.TaskTopDaoProvider
import com.gomgom.eod.feature.task.viewmodel.TaskTopUiState
import kotlinx.coroutines.flow.StateFlow

class TaskTopRepositoryImpl(
    private val dao: TaskTopDao = TaskTopDaoProvider.dao
) : TaskTopRepository {

    override val uiState: StateFlow<TaskTopUiState> = dao.uiState

    override fun setAlarmEnabled(enabled: Boolean) {
        dao.updateAlarmEnabled(enabled)
    }

    override fun setVesselEnabled(
        vesselId: Long,
        enabled: Boolean
    ) {
        dao.updateVesselEnabled(vesselId, enabled)
    }

    override fun addVessel(
        name: String,
        presetName: String
    ): Long? {
        return dao.addVessel(name, presetName)
    }

    override fun updateVesselName(
        vesselId: Long,
        name: String
    ) {
        dao.updateVesselName(vesselId, name)
    }

    override fun deleteVessel(vesselId: Long) {
        dao.deleteVessel(vesselId)
    }

    override fun addPresetGroup(name: String) {
        dao.addPresetGroup(name)
    }

    override fun setPresetGroupEnabled(
        presetId: Long,
        enabled: Boolean
    ) {
        dao.setPresetGroupEnabled(presetId, enabled)
    }

    override fun savePresetWork(
        presetId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        dao.savePresetWork(
            presetId = presetId,
            workName = workName,
            reference = reference,
            cycle = cycle
        )
    }

    override fun updatePresetWork(
        presetId: Long,
        workId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        dao.updatePresetWork(
            presetId = presetId,
            workId = workId,
            workName = workName,
            reference = reference,
            cycle = cycle
        )
    }

    override fun deletePresetWork(
        presetId: Long,
        workId: Long
    ) {
        dao.deletePresetWork(
            presetId = presetId,
            workId = workId
        )
    }

    override fun deletePresetGroup(presetId: Long) {
        dao.deletePresetGroup(presetId)
    }

    override fun clearAll() {
        dao.clearAll()
    }
}