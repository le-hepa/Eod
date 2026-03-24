package com.gomgom.eod.feature.task.dao

import androidx.room.withTransaction
import com.gomgom.eod.feature.task.db.TaskRoomDatabase
import com.gomgom.eod.feature.task.db.toEntity
import com.gomgom.eod.feature.task.db.toItem
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.viewmodel.TaskPresetGroupItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskTopUiState
import com.gomgom.eod.feature.task.viewmodel.TaskTopVesselItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TaskTopDaoImpl(
    private val alarmSettingsDao: TaskAlarmSettingsDao
) : TaskTopDao {
    private val roomDao by lazy { TaskRoomDatabase.getInstance().taskDao() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistMutex = Mutex()

    private val _uiState = MutableStateFlow(
        TaskTopUiState(
            alarmEnabled = alarmSettingsDao.settings.value.masterAlarmEnabled,
            vesselItems = emptyList()
        )
    )
    override val uiState: StateFlow<TaskTopUiState> = _uiState.asStateFlow()

    private var nextVesselId: Long = 1L
    private var nextPresetId: Long = 1L
    private var nextPresetWorkId: Long = 1L

    init {
        scope.launch {
            val loaded = loadVessels()
            nextVesselId = (loaded.maxOfOrNull { it.id } ?: 0L) + 1L
            _uiState.value = _uiState.value.copy(vesselItems = sortVessels(loaded))
        }
    }

    override fun updateAlarmEnabled(enabled: Boolean) {
        alarmSettingsDao.setMasterAlarmEnabled(enabled)
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
        persistVessels(_uiState.value.vesselItems)
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
        persistVessels(_uiState.value.vesselItems)

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
        persistVessels(_uiState.value.vesselItems)
    }

    override fun deleteVessel(vesselId: Long) {
        _uiState.value = _uiState.value.copy(
            vesselItems = sortVessels(
                _uiState.value.vesselItems.filterNot { it.id == vesselId }
            )
        )
        persistVessels(_uiState.value.vesselItems)
    }

    override fun addPresetGroup(name: String) {
        TaskPresetStateStore.addPreset(name)
    }

    override fun setPresetGroupEnabled(
        presetId: Long,
        enabled: Boolean
    ) {
        TaskPresetStateStore.setPresetEnabled(presetId, enabled)
    }

    override fun savePresetWork(
        presetId: Long,
        workName: String,
        reference: String,
        cycle: String
    ) {
        val trimmedWorkName = workName.trim()
        val trimmedReference = reference.trim()
        val parsedCycle = parseCycle(cycle) ?: return

        if (trimmedWorkName.isEmpty()) return

        TaskPresetWorkStateStore.addWork(
            presetId = presetId,
            name = trimmedWorkName,
            reference = trimmedReference,
            cycleNumber = parsedCycle.first,
            cycleUnit = parsedCycle.second
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
        val parsedCycle = parseCycle(cycle) ?: return

        if (trimmedWorkName.isEmpty()) return

        TaskPresetWorkStateStore.updateWork(
            workId = workId,
            name = trimmedWorkName,
            reference = trimmedReference,
            cycleNumber = parsedCycle.first,
            cycleUnit = parsedCycle.second
        )
    }

    override fun deletePresetWork(
        presetId: Long,
        workId: Long
    ) {
        TaskPresetWorkStateStore.deleteWork(workId)
    }

    override fun deletePresetGroup(presetId: Long) {
        TaskPresetStateStore.deletePreset(presetId)
    }

    override fun clearAll() {
        alarmSettingsDao.setMasterAlarmEnabled(false)
        _uiState.value = TaskTopUiState(alarmEnabled = false)
        nextVesselId = 1L
        nextPresetId = 1L
        nextPresetWorkId = 1L
        persistVessels(emptyList())
    }

    private fun sortVessels(items: List<TaskTopVesselItem>): List<TaskTopVesselItem> {
        return items.sortedWith(
            compareByDescending<TaskTopVesselItem> { it.enabled }
                .thenBy { it.name.lowercase() }
        )
    }

    private fun parseCycle(cycle: String): Pair<Int, CycleUnit>? {
        val trimmedCycle = cycle.trim()
        if (trimmedCycle.isEmpty()) return null

        val cycleNumber = trimmedCycle.takeWhile(Char::isDigit).toIntOrNull() ?: return null
        val cycleUnitToken = trimmedCycle.dropWhile(Char::isDigit).trim().uppercase()

        val cycleUnit = when (cycleUnitToken) {
            "D", "DAY", "DAYS" -> CycleUnit.DAY
            "W", "WEEK", "WEEKS" -> CycleUnit.WEEK
            "M", "MONTH", "MONTHS" -> CycleUnit.MONTH
            "Y", "YEAR", "YEARS" -> CycleUnit.YEAR
            else -> return null
        }

        return cycleNumber to cycleUnit
    }

    private suspend fun loadVessels(): List<TaskTopVesselItem> {
        TaskRoomDatabase.ensureMigrated()
        return roomDao.getVessels().map { it.toItem() }
    }

    private fun persistVessels(items: List<TaskTopVesselItem>) {
        scope.launch {
            persistMutex.withLock {
                TaskRoomDatabase.ensureMigrated()
                val roomDatabase = TaskRoomDatabase.getInstance()
                val existingEntities = roomDao.getVessels()
                val newEntities = items.map { it.toEntity() }
                val existingById = existingEntities.associateBy { it.id }
                val newById = newEntities.associateBy { it.id }
                val vesselsToUpsert = newEntities.filter { entity ->
                    existingById[entity.id]?.isSameContentAs(entity) != true
                }
                val vesselIdsToDelete = existingById.keys.filterNot(newById::containsKey)

                roomDatabase.withTransaction {
                    if (vesselIdsToDelete.isNotEmpty()) {
                        roomDao.deleteVesselsByIds(vesselIdsToDelete)
                    }
                    if (vesselsToUpsert.isNotEmpty()) {
                        roomDao.upsertVessels(vesselsToUpsert)
                    }
                }
            }
        }
    }
}

private fun com.gomgom.eod.feature.task.db.TaskVesselEntity.isSameContentAs(
    other: com.gomgom.eod.feature.task.db.TaskVesselEntity
): Boolean {
    return name == other.name &&
        presetName == other.presetName &&
        enabled == other.enabled
}
