package com.gomgom.eod.feature.task.repository

object TaskWorkRecordRepositoryProvider {
    val repository: TaskWorkRecordRepository by lazy {
        TaskWorkRecordRepositoryImpl()
    }
}
