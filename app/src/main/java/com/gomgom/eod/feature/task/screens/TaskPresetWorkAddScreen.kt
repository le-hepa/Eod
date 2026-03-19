package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import androidx.compose.runtime.collectAsState
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
    private val nextId = AtomicLong(1L)
    private val _works = MutableStateFlow<List<TaskPresetWorkItem>>(emptyList())
    val works = _works

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
        val trimmedName = name.trim()
        val trimmedReference = reference.trim()

        if (trimmedName.isBlank()) return null
        if (cycleNumber <= 0) return null

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

        return createdId
    }

    fun updateWork(
        workId: Long,
        name: String,
        reference: String,
        cycleNumber: Int,
        cycleUnit: CycleUnit
    ): Boolean {
        val trimmedName = name.trim()
        val trimmedReference = reference.trim()

        if (trimmedName.isBlank()) return false
        if (cycleNumber <= 0) return false

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

        return updated
    }

    fun deleteWork(workId: Long): Boolean {
        val before = _works.value.size
        _works.update { current -> current.filterNot { it.id == workId } }
        return _works.value.size != before
    }

    fun getWork(workId: Long): TaskPresetWorkItem? {
        return _works.value.firstOrNull { it.id == workId }
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

    fun getWork(workId: Long): TaskPresetWorkItem? {
        return TaskPresetWorkStateStore.getWork(workId)
    }
}