package com.gomgom.eod.feature.task.repository

import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import kotlinx.coroutines.flow.StateFlow

interface TaskAlarmSettingsRepository {
    val settings: StateFlow<TaskAlarmSettings>

    fun setMasterAlarmEnabled(enabled: Boolean)

    fun setAlarmTime(time: String)

    fun setRegularWorkAlarmEnabled(workId: Long, enabled: Boolean)

    fun isRegularWorkAlarmEnabled(workId: Long, defaultValue: Boolean = true): Boolean

    fun setIrregularWorkAlarmEnabled(vesselId: Long, workName: String, enabled: Boolean)

    fun isIrregularWorkAlarmEnabled(vesselId: Long, workName: String, defaultValue: Boolean = true): Boolean

    fun clearIrregularWorkAlarm(vesselId: Long, workName: String)

    fun clearAll()
}
