package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskAlarmSettingsDatabase
import com.gomgom.eod.feature.task.db.TaskAlarmSettingsEntity
import com.gomgom.eod.feature.task.db.TaskIrregularAlarmStateEntity
import com.gomgom.eod.feature.task.db.TaskRegularAlarmStateEntity
import com.gomgom.eod.feature.task.db.TaskRoomDatabase
import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TaskAlarmSettingsDaoImpl(
    private val database: TaskAlarmSettingsDatabase
) : TaskAlarmSettingsDao {
    private val roomDao by lazy { TaskRoomDatabase.getInstance().taskDao() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistMutex = Mutex()

    override val settings: StateFlow<TaskAlarmSettings> = database.state

    override fun setMasterAlarmEnabled(enabled: Boolean) {
        database.state.update { current -> current.copy(masterAlarmEnabled = enabled) }
        scope.launch {
            persistMutex.withLock {
                roomDao.upsertAlarmSettings(
                    TaskAlarmSettingsEntity(
                        masterAlarmEnabled = enabled,
                        alarmTime = database.state.value.alarmTime
                    )
                )
            }
        }
    }

    override fun setAlarmTime(time: String) {
        database.state.update { current -> current.copy(alarmTime = time) }
        scope.launch {
            persistMutex.withLock {
                roomDao.upsertAlarmSettings(
                    TaskAlarmSettingsEntity(
                        masterAlarmEnabled = database.state.value.masterAlarmEnabled,
                        alarmTime = time
                    )
                )
            }
        }
    }

    override fun setRegularWorkAlarmEnabled(workId: Long, enabled: Boolean) {
        database.state.update { current ->
            current.copy(
                regularWorkAlarmStates = current.regularWorkAlarmStates.toMutableMap().apply {
                    this[workId] = enabled
                }
            )
        }
        scope.launch {
            persistMutex.withLock {
                roomDao.clearRegularAlarmStates()
                roomDao.upsertRegularAlarmStates(
                    database.state.value.regularWorkAlarmStates.map {
                        TaskRegularAlarmStateEntity(workId = it.key, enabled = it.value)
                    }
                )
            }
        }
    }

    override fun setIrregularWorkAlarmEnabled(
        vesselId: Long,
        workName: String,
        enabled: Boolean
    ) {
        val key = buildIrregularKey(vesselId, workName)
        database.state.update { current ->
            current.copy(
                irregularWorkAlarmStates = current.irregularWorkAlarmStates.toMutableMap().apply {
                    this[key] = enabled
                }
            )
        }
        scope.launch {
            persistMutex.withLock {
                roomDao.clearIrregularAlarmStates()
                roomDao.upsertIrregularAlarmStates(
                    database.state.value.irregularWorkAlarmStates.map {
                        TaskIrregularAlarmStateEntity(key = it.key, enabled = it.value)
                    }
                )
            }
        }
    }

    override fun clearIrregularWorkAlarm(vesselId: Long, workName: String) {
        val key = buildIrregularKey(vesselId, workName)
        database.state.update { current ->
            current.copy(
                irregularWorkAlarmStates = current.irregularWorkAlarmStates.toMutableMap().apply {
                    remove(key)
                }
            )
        }
        scope.launch {
            persistMutex.withLock {
                roomDao.clearIrregularAlarmStates()
                roomDao.upsertIrregularAlarmStates(
                    database.state.value.irregularWorkAlarmStates.map {
                        TaskIrregularAlarmStateEntity(key = it.key, enabled = it.value)
                    }
                )
            }
        }
    }

    override fun clearAll() {
        database.state.value = TaskAlarmSettings()
        scope.launch {
            persistMutex.withLock {
                roomDao.upsertAlarmSettings(TaskAlarmSettingsEntity(masterAlarmEnabled = false, alarmTime = "08:00"))
                roomDao.clearRegularAlarmStates()
                roomDao.clearIrregularAlarmStates()
            }
        }
    }

    companion object {
        fun buildIrregularKey(vesselId: Long, workName: String): String {
            return "$vesselId|${workName.trim()}"
        }
    }
}
