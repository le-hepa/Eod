package com.gomgom.eod.feature.task.repository

object TaskAlarmSettingsRepositoryProvider {
    val repository: TaskAlarmSettingsRepository by lazy {
        TaskAlarmSettingsRepositoryImpl()
    }
}
