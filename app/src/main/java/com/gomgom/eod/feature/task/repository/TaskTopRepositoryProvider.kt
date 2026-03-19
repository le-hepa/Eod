package com.gomgom.eod.feature.task.repository

object TaskTopRepositoryProvider {
    val repository: TaskTopRepository by lazy {
        TaskTopRepositoryImpl()
    }
}