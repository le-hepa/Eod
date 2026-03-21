package com.gomgom.eod.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class TaskAlarmNavigationTarget(
    val vesselId: Long,
    val targetDate: LocalDate,
    val targetRecordId: Long?
)

object TaskAlarmNavigationBridge {
    private val _target = MutableStateFlow<TaskAlarmNavigationTarget?>(null)
    val target = _target.asStateFlow()

    fun deliver(target: TaskAlarmNavigationTarget) {
        _target.value = target
    }

    fun clear() {
        _target.value = null
    }
}
