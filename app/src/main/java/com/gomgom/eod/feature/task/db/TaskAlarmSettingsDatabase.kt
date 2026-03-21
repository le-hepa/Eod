package com.gomgom.eod.feature.task.db

import android.content.Context
import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TaskAlarmSettingsDatabase(
    private val context: Context
) {
    private val roomDao by lazy { TaskRoomDatabase.getInstance(context).taskDao() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val state = MutableStateFlow(TaskAlarmSettings())

    init {
        scope.launch {
            state.value = loadSettings()
        }
    }

    private suspend fun loadSettings(): TaskAlarmSettings {
        TaskRoomDatabase.ensureMigrated(context)
        val settingsEntity = roomDao.getAlarmSettings()
            ?: TaskAlarmSettingsEntity(masterAlarmEnabled = false, alarmTime = DEFAULT_ALARM_TIME)
        val regularStates = roomDao.getRegularAlarmStates().associate { it.workId to it.enabled }
        val irregularStates = roomDao.getIrregularAlarmStates().associate { it.key to it.enabled }
        return settingsEntity.toModel(
            regularStates = regularStates,
            irregularStates = irregularStates
        )
    }

    companion object {
        const val DEFAULT_ALARM_TIME = "08:00"

        fun encodeLongBooleanMap(map: Map<Long, Boolean>): Set<String> {
            return map.map { (key, value) -> "$key|${if (value) 1 else 0}" }.toSet()
        }

        fun decodeLongBooleanMap(values: Set<String>): Map<Long, Boolean> {
            return values.mapNotNull { raw ->
                val parts = raw.split("|")
                if (parts.size != 2) return@mapNotNull null
                val key = parts[0].toLongOrNull() ?: return@mapNotNull null
                key to (parts[1] == "1")
            }.toMap()
        }

        fun encodeStringBooleanMap(map: Map<String, Boolean>): Set<String> {
            return map.map { (key, value) -> "$key|${if (value) 1 else 0}" }.toSet()
        }

        fun decodeStringBooleanMap(values: Set<String>): Map<String, Boolean> {
            return values.mapNotNull { raw ->
                val splitIndex = raw.lastIndexOf('|')
                if (splitIndex <= 0 || splitIndex >= raw.lastIndex) return@mapNotNull null
                val key = raw.substring(0, splitIndex)
                key to (raw.substring(splitIndex + 1) == "1")
            }.toMap()
        }
    }
}
