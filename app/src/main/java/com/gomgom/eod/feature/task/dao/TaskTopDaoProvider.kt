package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskDatabaseProvider

object TaskTopDaoProvider {
    val dao: TaskTopDao by lazy {
        TaskDatabaseProvider.database.taskTopDao
    }
}