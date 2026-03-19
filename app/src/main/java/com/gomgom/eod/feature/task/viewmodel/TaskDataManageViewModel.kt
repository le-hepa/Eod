package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.repository.TaskTopRepository
import com.gomgom.eod.feature.task.repository.TaskTopRepositoryProvider

class TaskDataManageViewModel(
    private val repository: TaskTopRepository = TaskTopRepositoryProvider.repository
) : ViewModel() {

    fun clearAll() {
        repository.clearAll()
    }
}