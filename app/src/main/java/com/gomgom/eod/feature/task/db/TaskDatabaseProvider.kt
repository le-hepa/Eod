package com.gomgom.eod.feature.task.db

import com.gomgom.eod.feature.task.dao.TaskTopDaoImpl

object TaskDatabaseProvider {
    val database: TaskDatabase by lazy {
        TaskDatabase(
            taskTopDao = TaskTopDaoImpl()
        )
    }
}