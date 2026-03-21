package com.gomgom.eod.feature.task.model

import java.time.LocalDate

enum class TaskWorkRecordType {
    REGULAR,
    IRREGULAR
}

enum class TaskWorkRecordStatus {
    RegularPlanned,
    RegularDelayed,
    RegularDone,
    NonRegularPlanned,
    NonRegularDelayed,
    NonRegularDone
}

enum class TaskWorkRecordAttachmentType {
    IMAGE,
    VIDEO
}

data class TaskWorkRecordAttachmentItem(
    val id: Long,
    val recordId: Long,
    val type: TaskWorkRecordAttachmentType,
    val uri: String,
    val displayName: String = ""
)

data class TaskWorkRecordItem(
    val id: Long,
    val vesselId: Long,
    val recordDate: LocalDate,
    val presetWorkId: Long?,
    val recordType: TaskWorkRecordType,
    val workName: String,
    val reference: String,
    val cycleNumberText: String,
    val cycleUnitText: String,
    val status: TaskWorkRecordStatus,
    val comment: String,
    val attachments: List<TaskWorkRecordAttachmentItem> = emptyList()
)
