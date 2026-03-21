package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import kotlinx.coroutines.flow.StateFlow

interface TaskAlarmSettingsDao {
    val settings: StateFlow<TaskAlarmSettings>

    fun setMasterAlarmEnabled(enabled: Boolean)

    fun setAlarmTime(time: String)

    fun setRegularWorkAlarmEnabled(workId: Long, enabled: Boolean)

    fun setIrregularWorkAlarmEnabled(
        vesselId: Long,
        workName: String,
        enabled: Boolean
    )

    fun clearIrregularWorkAlarm(vesselId: Long, workName: String)

    fun clearAll()
}
