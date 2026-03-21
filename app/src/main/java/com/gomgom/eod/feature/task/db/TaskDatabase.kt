package com.gomgom.eod.feature.task.db

import com.gomgom.eod.feature.task.dao.TaskTopDao
import com.gomgom.eod.feature.task.dao.TaskAlarmSettingsDao
import com.gomgom.eod.feature.task.dao.TaskWorkRecordDao

class TaskDatabase(
    taskTopDaoFactory: () -> TaskTopDao,
    taskWorkRecordDaoFactory: () -> TaskWorkRecordDao,
    taskAlarmSettingsDaoFactory: () -> TaskAlarmSettingsDao
) {
    val taskTopDao: TaskTopDao by lazy(taskTopDaoFactory)
    val taskWorkRecordDao: TaskWorkRecordDao by lazy(taskWorkRecordDaoFactory)
    val taskAlarmSettingsDao: TaskAlarmSettingsDao by lazy(taskAlarmSettingsDaoFactory)
}
