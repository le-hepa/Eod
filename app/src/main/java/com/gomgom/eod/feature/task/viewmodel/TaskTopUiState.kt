package com.gomgom.eod.feature.task.viewmodel

data class TaskTopUiState(
    val alarmEnabled: Boolean = false,
    val vesselItems: List<TaskTopVesselItem> = emptyList(),
    val presetGroups: List<TaskPresetGroupItem> = emptyList()
)

data class TaskTopVesselItem(
    val id: Long,
    val name: String,
    val presetName: String,
    val enabled: Boolean = false
)

data class TaskPresetGroupItem(
    val id: Long,
    val name: String,
    val enabled: Boolean = false,
    val works: List<TaskPresetWorkItem> = emptyList()
)
