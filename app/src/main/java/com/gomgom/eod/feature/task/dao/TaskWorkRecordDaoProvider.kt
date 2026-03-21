package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskDatabaseProvider

object TaskWorkRecordDaoProvider {
    val dao: TaskWorkRecordDao by lazy {
        TaskDatabaseProvider.database.taskWorkRecordDao
    }
}
