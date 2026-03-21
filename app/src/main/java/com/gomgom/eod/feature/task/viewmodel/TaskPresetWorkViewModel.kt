package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.db.TaskPresetWorkEntity
import com.gomgom.eod.feature.task.db.TaskRoomDatabase
import com.gomgom.eod.feature.task.db.toItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

data class TaskPresetWorkItem(
    val id: Long,
    val presetId: Long,
    val name: String,
    val reference: String,
    val cycleNumber: Int,
    val cycleUnit: CycleUnit,
    val alarmEnabled: Boolean = true
)

enum class CycleUnit {
    DAY, WEEK, MONTH, YEAR
}

object TaskPresetWorkStateStore {
    private val roomDao by lazy { TaskRoomDatabase.getInstance().taskDao() }
    private val nextId = AtomicLong(1L)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistMutex = Mutex()
    @Volatile
    private var loaded = false
    private val _works = MutableStateFlow<List<TaskPresetWorkItem>>(emptyList())
    val works = _works

    init {
        scope.launch {
            val items = loadWorks()
            nextId.set((items.maxOfOrNull { it.id } ?: 0L) + 1L)
            _works.value = items
            loaded = true
        }
    }

    fun worksForPreset(presetId: Long): Flow<List<TaskPresetWorkItem>> {
        return works.map { all ->
            all.filter { it.presetId == presetId }
                .sortedBy { it.id }
        }
    }

    fun addWork(
        presetId: Long,
        name: String,
        reference: String,
        cycleNumber: Int,
        cycleUnit: CycleUnit
    ): Long? {
        ensureLoadedForMutation()
        val trimmedName = name.trim()
        val trimmedReference = reference.trim()

        if (trimmedName.isBlank()) return null
        if (cycleNumber <= 0) return null
        if (isDuplicateNameInPreset(presetId = presetId, name = trimmedName)) return null

        val createdId = nextId.getAndIncrement()

        _works.update { current ->
            current + TaskPresetWorkItem(
                id = createdId,
                presetId = presetId,
                name = trimmedName,
                reference = trimmedReference,
                cycleNumber = cycleNumber,
                cycleUnit = cycleUnit,
                alarmEnabled = true
            )
        }
        persist()
        return createdId
    }

    fun updateWork(
        workId: Long,
        name: String,
        reference: String,
        cycleNumber: Int,
        cycleUnit: CycleUnit
    ): Boolean {
        ensureLoadedForMutation()
        val trimmedName = name.trim()
        val trimmedReference = reference.trim()

        if (trimmedName.isBlank()) return false
        if (cycleNumber <= 0) return false
        val targetWork = getWork(workId) ?: return false
        if (isDuplicateNameInPreset(presetId = targetWork.presetId, name = trimmedName, excludeWorkId = workId)) {
            return false
        }

        var updated = false

        _works.update { current ->
            current.map { item ->
                if (item.id == workId) {
                    updated = true
                    item.copy(
                        name = trimmedName,
                        reference = trimmedReference,
                        cycleNumber = cycleNumber,
                        cycleUnit = cycleUnit
                    )
                } else {
                    item
                }
            }
        }
        if (updated) persist()
        return updated
    }

    fun deleteWork(workId: Long): Boolean {
        ensureLoadedForMutation()
        val before = _works.value.size
        _works.update { current -> current.filterNot { it.id == workId } }
        val deleted = _works.value.size != before
        if (deleted) persist()
        return deleted
    }

    fun updateAlarmEnabled(
        workId: Long,
        enabled: Boolean
    ): Boolean {
        ensureLoadedForMutation()
        var updated = false

        _works.update { current ->
            current.map { item ->
                if (item.id == workId) {
                    updated = true
                    item.copy(alarmEnabled = enabled)
                } else {
                    item
                }
            }
        }
        if (updated) persist()
        return updated
    }

    fun getWork(workId: Long): TaskPresetWorkItem? {
        ensureLoadedForMutation()
        return _works.value.firstOrNull { it.id == workId }
    }

    fun isDuplicateNameInPreset(
        presetId: Long,
        name: String,
        excludeWorkId: Long? = null
    ): Boolean {
        ensureLoadedForMutation()
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return false

        return _works.value.any { item ->
            item.presetId == presetId &&
                item.id != excludeWorkId &&
                item.name.trim() == trimmedName
        }
    }

    fun snapshot(): List<TaskPresetWorkItem> = _works.value

    fun replaceAll(items: List<TaskPresetWorkItem>) {
        loaded = true
        _works.value = items.sortedBy { it.id }
        nextId.set((items.maxOfOrNull { it.id } ?: 0L) + 1L)
        persist()
    }

    fun clearAll() {
        loaded = true
        _works.value = emptyList()
        nextId.set(1L)
        persist()
    }

    private fun loadWorks(): List<TaskPresetWorkItem> {
        return runBlocking(Dispatchers.IO) {
            TaskRoomDatabase.ensureMigrated()
            roomDao.getPresetWorks()
        }
            .map { it.toItem() }
            .sortedBy { it.id }
    }

    private fun persist() {
        scope.launch {
            persistMutex.withLock {
                TaskRoomDatabase.ensureMigrated()
                roomDao.clearPresetWorks()
                roomDao.upsertPresetWorks(
                    _works.value.map {
                        TaskPresetWorkEntity(
                            id = it.id,
                            presetId = it.presetId,
                            name = it.name,
                            reference = it.reference,
                            cycleNumber = it.cycleNumber,
                            cycleUnit = it.cycleUnit.name,
                            alarmEnabled = it.alarmEnabled
                        )
                    }
                )
            }
        }
    }

    private fun ensureLoadedForMutation() {
        if (loaded) return
        val items = loadWorks()
        nextId.set((items.maxOfOrNull { it.id } ?: 0L) + 1L)
        _works.value = items
        loaded = true
    }
}

class TaskPresetWorkViewModel : ViewModel() {

    fun worksForPreset(presetId: Long): Flow<List<TaskPresetWorkItem>> {
        return TaskPresetWorkStateStore.worksForPreset(presetId)
    }

    fun addWork(
        presetId: Long,
        name: String,
        reference: String,
        cycleNumber: Int,
        cycleUnit: CycleUnit
    ): Long? {
        return TaskPresetWorkStateStore.addWork(
            presetId = presetId,
            name = name,
            reference = reference,
            cycleNumber = cycleNumber,
            cycleUnit = cycleUnit
        )
    }

    fun updateWork(
        workId: Long,
        name: String,
        reference: String,
        cycleNumber: Int,
        cycleUnit: CycleUnit
    ): Boolean {
        return TaskPresetWorkStateStore.updateWork(
            workId = workId,
            name = name,
            reference = reference,
            cycleNumber = cycleNumber,
            cycleUnit = cycleUnit
        )
    }

    fun deleteWork(workId: Long): Boolean {
        return TaskPresetWorkStateStore.deleteWork(workId)
    }

    fun updateAlarmEnabled(
        workId: Long,
        enabled: Boolean
    ): Boolean {
        return TaskPresetWorkStateStore.updateAlarmEnabled(
            workId = workId,
            enabled = enabled
        )
    }

    fun getWork(workId: Long): TaskPresetWorkItem? {
        return TaskPresetWorkStateStore.getWork(workId)
    }

    fun isDuplicateNameInPreset(
        presetId: Long,
        name: String,
        excludeWorkId: Long? = null
    ): Boolean {
        return TaskPresetWorkStateStore.isDuplicateNameInPreset(
            presetId = presetId,
            name = name,
            excludeWorkId = excludeWorkId
        )
    }
}
