package com.gomgom.eod.feature.task.model

data class TaskAlarmSettings(
    val masterAlarmEnabled: Boolean = false,
    val alarmTime: String = "08:00",
    val regularWorkAlarmStates: Map<Long, Boolean> = emptyMap(),
    val irregularWorkAlarmStates: Map<String, Boolean> = emptyMap()
)
