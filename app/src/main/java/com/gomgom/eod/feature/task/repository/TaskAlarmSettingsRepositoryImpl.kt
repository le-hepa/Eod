package com.gomgom.eod.feature.task.repository

import com.gomgom.eod.EodApp
import com.gomgom.eod.feature.task.alarm.TaskAlarmScheduler
import com.gomgom.eod.feature.task.dao.TaskAlarmSettingsDao
import com.gomgom.eod.feature.task.dao.TaskAlarmSettingsDaoProvider
import com.gomgom.eod.feature.task.dao.TaskAlarmSettingsDaoImpl
import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import kotlinx.coroutines.flow.StateFlow

class TaskAlarmSettingsRepositoryImpl(
    private val dao: TaskAlarmSettingsDao = TaskAlarmSettingsDaoProvider.dao
) : TaskAlarmSettingsRepository {

    override val settings: StateFlow<TaskAlarmSettings> = dao.settings

    override fun setMasterAlarmEnabled(enabled: Boolean) {
        dao.setMasterAlarmEnabled(enabled)
    }

    override fun setAlarmTime(time: String) {
        dao.setAlarmTime(time)
    }

    override fun setRegularWorkAlarmEnabled(workId: Long, enabled: Boolean) {
        dao.setRegularWorkAlarmEnabled(workId, enabled)
        TaskTopRepositoryProvider.repository.uiState.value.vesselItems
            .filter { it.enabled }
            .forEach { vessel ->
                TaskAlarmScheduler.syncForRegularWork(EodApp.appContext, vessel.id, workId)
            }
    }

    override fun isRegularWorkAlarmEnabled(workId: Long, defaultValue: Boolean): Boolean {
        return settings.value.regularWorkAlarmStates[workId] ?: defaultValue
    }

    override fun setIrregularWorkAlarmEnabled(vesselId: Long, workName: String, enabled: Boolean) {
        dao.setIrregularWorkAlarmEnabled(vesselId, workName, enabled)
        TaskAlarmScheduler.syncForIrregularWork(EodApp.appContext, vesselId, workName)
    }

    override fun isIrregularWorkAlarmEnabled(vesselId: Long, workName: String, defaultValue: Boolean): Boolean {
        val key = TaskAlarmSettingsDaoImpl.buildIrregularKey(vesselId, workName)
        return settings.value.irregularWorkAlarmStates[key] ?: defaultValue
    }

    override fun clearIrregularWorkAlarm(vesselId: Long, workName: String) {
        dao.clearIrregularWorkAlarm(vesselId, workName)
        TaskAlarmScheduler.syncForIrregularWork(EodApp.appContext, vesselId, workName)
    }

    override fun clearAll() {
        dao.clearAll()
    }
}
