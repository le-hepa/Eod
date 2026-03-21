package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gomgom.eod.feature.task.db.TaskRoomDatabase
import com.gomgom.eod.feature.task.db.TaskPresetGroupEntity
import com.gomgom.eod.feature.task.db.toItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

object TaskPresetStateStore {
    private val roomDao by lazy { TaskRoomDatabase.getInstance().taskDao() }
    private val nextId = AtomicLong(1L)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistMutex = Mutex()
    @Volatile
    private var loaded = false

    private val _presetGroups = MutableStateFlow<List<TaskPresetGroupItem>>(emptyList())
    val presetGroups = _presetGroups

    init {
        scope.launch {
            val items = loadPresetGroups()
            nextId.set((items.maxOfOrNull { it.id } ?: 0L) + 1L)
            _presetGroups.value = items
            loaded = true
        }
    }

    fun addPreset(name: String): Long? {
        ensureLoadedForMutation()
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
        persist()

        return createdId
    }

    fun setPresetEnabled(presetId: Long, enabled: Boolean) {
        ensureLoadedForMutation()
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
        persist()
    }

    fun renamePreset(presetId: Long, name: String): Boolean {
        ensureLoadedForMutation()
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        var updated = false
        _presetGroups.update { current ->
            current.map { item ->
                if (item.id == presetId) {
                    updated = true
                    item.copy(name = trimmed)
                } else {
                    item
                }
            }
        }
        if (updated) persist()
        return updated
    }

    fun deletePreset(presetId: Long): Boolean {
        ensureLoadedForMutation()
        val current = _presetGroups.value
        val target = current.firstOrNull { it.id == presetId } ?: return false
        val remaining = current.filterNot { it.id == presetId }
        _presetGroups.value = if (target.enabled && remaining.isNotEmpty()) {
            remaining.mapIndexed { index, item ->
                item.copy(enabled = index == 0)
            }
        } else {
            remaining
        }
        persist()
        return true
    }

    fun snapshot(): List<TaskPresetGroupItem> = _presetGroups.value

    fun replaceAll(items: List<TaskPresetGroupItem>) {
        loaded = true
        val normalized = if (items.any { it.enabled }) {
            items.mapIndexed { index, item ->
                if (items.count { it.enabled } > 1 && item.enabled && index != items.indexOfFirst { it.enabled }) {
                    item.copy(enabled = false)
                } else {
                    item
                }
            }
        } else if (items.isNotEmpty()) {
            items.mapIndexed { index, item -> item.copy(enabled = index == 0) }
        } else {
            emptyList()
        }
        _presetGroups.value = normalized
        nextId.set((normalized.maxOfOrNull { it.id } ?: 0L) + 1L)
        persist()
    }

    fun clearAll() {
        loaded = true
        _presetGroups.value = emptyList()
        nextId.set(1L)
        persist()
    }

    private fun loadPresetGroups(): List<TaskPresetGroupItem> {
        return runBlocking(Dispatchers.IO) {
            TaskRoomDatabase.ensureMigrated()
            roomDao.getPresetGroups()
        }
            .map { it.toItem(emptyList()) }
    }

    private fun persist() {
        scope.launch {
            persistMutex.withLock {
                TaskRoomDatabase.ensureMigrated()
                roomDao.clearPresetGroups()
                roomDao.upsertPresetGroups(
                    _presetGroups.value.map {
                        TaskPresetGroupEntity(
                            id = it.id,
                            name = it.name,
                            enabled = it.enabled
                        )
                    }
                )
            }
        }
    }

    private fun ensureLoadedForMutation() {
        if (loaded) return
        val items = loadPresetGroups()
        nextId.set((items.maxOfOrNull { it.id } ?: 0L) + 1L)
        _presetGroups.value = items
        loaded = true
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

    fun renamePreset(presetId: Long, name: String): Boolean {
        return TaskPresetStateStore.renamePreset(presetId, name)
    }

    fun deletePreset(presetId: Long): Boolean {
        return TaskPresetStateStore.deletePreset(presetId)
    }
}
