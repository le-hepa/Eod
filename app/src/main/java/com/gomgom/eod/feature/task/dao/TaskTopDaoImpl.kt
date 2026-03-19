package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.viewmodel.TaskPresetGroupItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskTopUiState
import com.gomgom.eod.feature.task.viewmodel.TaskTopVesselItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskTopDaoImpl : TaskTopDao {

    private val _uiState = MutableStateFlow(TaskTopUiState())
    override val uiState: StateFlow<TaskTopUiState> = _uiState.asStateFlow()

    private var nextVesselId: Long = 1L
    private var nextPresetId: Long = 1L
    private var nextPresetWorkId: Long = 1L

    override fun updateAlarmEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(alarmEnabled = enabled)
    }

    override fun updateVesselEnabled(
        vesselId: Long,
        enabled: Boolean
    ) {
        val updated = if (enabled) {
            _uiState.value.vesselItems.map { item ->
                when (item.id) {
                    vesselId -> item.copy(enabled = true)
                    else -> item.copy(enabled = false)
                }
            }
        } else {
            _uiState.value.vesselItems.map { item ->
                if (item.id == vesselId) item.copy(enabled = false) else item
            }
        }

        _uiState.value = _uiState.value.copy(
            vesselItems = sortVessels(updated)
        )
    }

    override fun addVessel(
        name: String,
        presetName: String
    ): Long? {
        val trimmedName = name
            .trim()
            .replace(Regex("\\s+"), " ")

        val trimmedPresetName = presetName.trim()

        if (trimmedName.isEmpty()) return null
        if (trimmedPresetName.isEmpty()) return null

        val newId = nextVesselId++

        val newItem = TaskTopVesselItem(
            id = newId,
            name = trimmedName,
            presetName = trimmedPresetName,
            enabled = true
        )

        val updated = _uiState.value.vesselItems.map { it.copy(enabled = false) } + newItem

        _uiState.value = _uiState.value.copy(
            vesselItems = sortVessels(updated)
        )

        return newId
    }

    override fun updateVesselName(
        vesselId: Long,
        name: String
    ) {
        val trimmedName = name
            .trim()
            .replace(Regex("\\s+"), " ")

        if (trimmedName.isEmpty()) return

        val updated = _uiState.value.vesselItems.map { item ->
            if (item.id == vesselId) item.copy(name = trimmedName) else item
        }

        _uiState.value = _uiState.value.copy(
            vesselItems = sortVessels(updated)
        )
    }

    override fun deleteVessel(vesselId: Long) {
        _uiState.value = _uiState.value.copy(
            vesselItems = sortVessels(
                _uiState.value.vesselItems.filterNot { it.id == vesselId }
            )
        )
    }

    override fun addPresetGroup(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups + TaskPresetGroupItem(
                id = nextPresetId++,
                name = trimmedName,
                enabled = false,
                works = emptyList()
            )
        )
    }

    override fun setPresetGroupEnabled(
        presetId: Long,
        enabled: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups.map { item ->
                when {
                    item.id == presetId -> item.copy(enabled = enabled)
                    enabled -> item.copy(enabled = false)
                    else -> item
                }
            }
        )
    }

    override fun savePresetWork(
        presetId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        val trimmedWorkName = workName.trim()
        val trimmedReference = reference.trim()
        val trimmedCycle = cycle.trim()

        if (trimmedWorkName.isEmpty()) return
        if (trimmedCycle.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups.map { item ->
                if (item.id == presetId) {
                    item.copy(
                        works = item.works + TaskPresetWorkItem(
                            id = nextPresetWorkId++,
                            workName = trimmedWorkName,
                            reference = trimmedReference,
                            cycle = trimmedCycle
                        )
                    )
                } else {
                    item
                }
            }
        )
    }

    override fun updatePresetWork(
        presetId: Long,
        workId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        val trimmedWorkName = workName.trim()
        val trimmedReference = reference.trim()
        val trimmedCycle = cycle.trim()

        if (trimmedWorkName.isEmpty()) return
        if (trimmedCycle.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups.map { item ->
                if (item.id == presetId) {
                    item.copy(
                        works = item.works.map { work ->
                            if (work.id == workId) {
                                work.copy(
                                    workName = trimmedWorkName,
                                    reference = trimmedReference,
                                    cycle = trimmedCycle
                                )
                            } else {
                                work
                            }
                        }
                    )
                } else {
                    item
                }
            }
        )
    }

    override fun deletePresetWork(
        presetId: Long,
        workId: Long
    ) {
        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups.map { item ->
                if (item.id == presetId) {
                    item.copy(
                        works = item.works.filterNot { it.id == workId }
                    )
                } else {
                    item
                }
            }
        )
    }

    override fun deletePresetGroup(presetId: Long) {
        _uiState.value = _uiState.value.copy(
            presetGroups = _uiState.value.presetGroups.filterNot { it.id == presetId }
        )
    }

    override fun clearAll() {
        _uiState.value = TaskTopUiState()
        nextVesselId = 1L
        nextPresetId = 1L
        nextPresetWorkId = 1L
    }

    private fun sortVessels(items: List<TaskTopVesselItem>): List<TaskTopVesselItem> {
        return items.sortedWith(
            compareByDescending<TaskTopVesselItem> { it.enabled }
                .thenBy { it.name.lowercase() }
        )
    }
}