package com.gomgom.eod.feature.task.db

import com.gomgom.eod.EodApp
import com.gomgom.eod.feature.task.dao.TaskAlarmSettingsDaoImpl
import com.gomgom.eod.feature.task.dao.TaskTopDaoImpl
import com.gomgom.eod.feature.task.dao.TaskWorkRecordDaoImpl

object TaskDatabaseProvider {
    val database: TaskDatabase by lazy {
        val alarmSettingsDatabase = TaskAlarmSettingsDatabase(EodApp.appContext)
        val alarmSettingsDao = TaskAlarmSettingsDaoImpl(
            database = alarmSettingsDatabase
        )
        TaskDatabase(
            taskTopDaoFactory = {
                TaskTopDaoImpl(alarmSettingsDao = alarmSettingsDao)
            },
            taskWorkRecordDaoFactory = {
                TaskWorkRecordDaoImpl(
                    database = TaskWorkRecordDatabase()
                )
            },
            taskAlarmSettingsDaoFactory = {
                alarmSettingsDao
            }
        )
    }
}
