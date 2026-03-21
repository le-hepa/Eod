package com.gomgom.eod.feature.task.repository

import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import com.gomgom.eod.feature.task.viewmodel.CycleUnit

data class TaskAlarmDisplayItem(
    val id: Long,
    val name: String,
    val cycleNumber: Int?,
    val cycleUnit: CycleUnit?,
    val alarmEnabled: Boolean,
    val type: TaskAlarmDisplayType
)

enum class TaskAlarmDisplayType {
    REGULAR,
    IRREGULAR
}

enum class TaskAlarmDisplaySort {
    BASIC,
    A_TO_Z,
    Z_TO_A,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

enum class TaskAlarmDisplayFilter {
    ALL,
    REGULAR,
    IRREGULAR
}

interface TaskWorkRecordRepository {
    fun allRecords(): List<TaskWorkRecordItem>

    fun recordsForVessel(vesselId: Long): Flow<List<TaskWorkRecordItem>>

    fun recordsGroupedByDate(vesselId: Long): Flow<Map<LocalDate, List<TaskWorkRecordItem>>>

    fun recordsForDate(vesselId: Long, recordDate: LocalDate): Flow<List<TaskWorkRecordItem>>

    fun recordsForDateByType(
        vesselId: Long,
        recordDate: LocalDate,
        recordType: TaskWorkRecordType
    ): Flow<List<TaskWorkRecordItem>>

    fun listRecordsForVessel(
        vesselId: Long,
        recordDate: LocalDate,
        includeAllDates: Boolean,
        query: String
    ): Flow<List<TaskWorkRecordItem>>

    fun recentRecordsFor(
        vesselId: Long,
        recordType: TaskWorkRecordType,
        presetWorkId: Long?,
        workName: String
    ): Flow<List<TaskWorkRecordItem>>

    fun latestRegularRecordForWork(vesselId: Long, presetWorkId: Long): TaskWorkRecordItem?

    fun irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem>

    fun irregularAlarmCandidatesForVesselFlow(vesselId: Long): Flow<List<TaskWorkRecordItem>>

    fun buildAlarmVisibleItems(
        items: List<TaskAlarmDisplayItem>,
        filter: TaskAlarmDisplayFilter,
        sort: TaskAlarmDisplaySort
    ): List<TaskAlarmDisplayItem>

    fun saveRecord(
        recordId: Long?,
        vesselId: Long,
        recordDate: LocalDate,
        presetWorkId: Long?,
        recordType: TaskWorkRecordType,
        workName: String,
        reference: String,
        cycleNumberText: String,
        cycleUnitText: String,
        status: TaskWorkRecordStatus,
        comment: String,
        attachments: List<TaskWorkRecordAttachmentItem>
    ): Long?

    fun getRecord(recordId: Long): TaskWorkRecordItem?

    fun updateRecordStatus(
        recordId: Long,
        status: TaskWorkRecordStatus
    ): Boolean

    fun deleteRecord(recordId: Long): Boolean

    fun clearAll()
}
