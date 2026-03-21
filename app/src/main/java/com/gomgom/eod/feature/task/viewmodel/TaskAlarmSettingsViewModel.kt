package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import com.gomgom.eod.feature.task.repository.TaskAlarmSettingsRepository
import com.gomgom.eod.feature.task.repository.TaskAlarmSettingsRepositoryProvider
import kotlinx.coroutines.flow.StateFlow

class TaskAlarmSettingsViewModel(
    private val repository: TaskAlarmSettingsRepository = TaskAlarmSettingsRepositoryProvider.repository
) : ViewModel() {

    val settings: StateFlow<TaskAlarmSettings> = repository.settings

    fun setAlarmTime(time: String) {
        repository.setAlarmTime(time)
    }

    fun setRegularWorkAlarmEnabled(workId: Long, enabled: Boolean) {
        repository.setRegularWorkAlarmEnabled(workId, enabled)
    }

    fun isRegularWorkAlarmEnabled(workId: Long, defaultValue: Boolean = true): Boolean {
        return repository.isRegularWorkAlarmEnabled(workId, defaultValue)
    }

    fun setIrregularWorkAlarmEnabled(vesselId: Long, workName: String, enabled: Boolean) {
        repository.setIrregularWorkAlarmEnabled(vesselId, workName, enabled)
    }

    fun isIrregularWorkAlarmEnabled(vesselId: Long, workName: String, defaultValue: Boolean = true): Boolean {
        return repository.isIrregularWorkAlarmEnabled(vesselId, workName, defaultValue)
    }

    fun clearIrregularWorkAlarm(vesselId: Long, workName: String) {
        repository.clearIrregularWorkAlarm(vesselId, workName)
    }
}
