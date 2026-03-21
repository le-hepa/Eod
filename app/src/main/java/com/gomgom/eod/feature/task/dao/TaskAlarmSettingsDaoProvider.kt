package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskDatabaseProvider

object TaskAlarmSettingsDaoProvider {
    val dao: TaskAlarmSettingsDao by lazy {
        TaskDatabaseProvider.database.taskAlarmSettingsDao
    }
}
