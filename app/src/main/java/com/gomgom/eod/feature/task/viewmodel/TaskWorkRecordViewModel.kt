package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplayFilter
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplayItem
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplaySort
import com.gomgom.eod.feature.task.repository.TaskWorkRecordRepository
import com.gomgom.eod.feature.task.repository.TaskWorkRecordRepositoryProvider
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskWorkRecordViewModel(
    private val repository: TaskWorkRecordRepository = TaskWorkRecordRepositoryProvider.repository
) : ViewModel() {

    fun allRecords(): List<TaskWorkRecordItem> {
        return repository.allRecords()
    }

    fun recordsForVessel(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return repository.recordsForVessel(vesselId)
    }

    fun recordsGroupedByDate(vesselId: Long): Flow<Map<LocalDate, List<TaskWorkRecordItem>>> {
        return repository.recordsGroupedByDate(vesselId)
    }

    fun recordsForDate(vesselId: Long, recordDate: LocalDate): Flow<List<TaskWorkRecordItem>> {
        return repository.recordsForDate(vesselId, recordDate)
    }

    fun recordsForDateByType(
        vesselId: Long,
        recordDate: LocalDate,
        recordType: TaskWorkRecordType
    ): Flow<List<TaskWorkRecordItem>> {
        return repository.recordsForDateByType(
            vesselId = vesselId,
            recordDate = recordDate,
            recordType = recordType
        )
    }

    fun listRecordsForVessel(
        vesselId: Long,
        recordDate: LocalDate,
        includeAllDates: Boolean,
        query: String
    ): Flow<List<TaskWorkRecordItem>> {
        return repository.listRecordsForVessel(
            vesselId = vesselId,
            recordDate = recordDate,
            includeAllDates = includeAllDates,
            query = query
        )
    }

    fun recentRecordsFor(
        vesselId: Long,
        recordType: TaskWorkRecordType,
        presetWorkId: Long?,
        workName: String
    ): Flow<List<TaskWorkRecordItem>> {
        return repository.recentRecordsFor(
            vesselId = vesselId,
            recordType = recordType,
            presetWorkId = presetWorkId,
            workName = workName
        )
    }

    fun latestRegularRecordForWork(vesselId: Long, presetWorkId: Long): TaskWorkRecordItem? {
        return repository.latestRegularRecordForWork(vesselId, presetWorkId)
    }

    fun irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem> {
        return repository.irregularAlarmCandidatesForVessel(vesselId)
    }

    fun irregularAlarmCandidatesForVesselFlow(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return repository.irregularAlarmCandidatesForVesselFlow(vesselId)
    }

    fun buildAlarmVisibleItems(
        items: List<TaskAlarmDisplayItem>,
        filter: TaskAlarmDisplayFilter,
        sort: TaskAlarmDisplaySort
    ): List<TaskAlarmDisplayItem> {
        return repository.buildAlarmVisibleItems(
            items = items,
            filter = filter,
            sort = sort
        )
    }

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
    ): Long? {
        return repository.saveRecord(
            recordId = recordId,
            vesselId = vesselId,
            recordDate = recordDate,
            presetWorkId = presetWorkId,
            recordType = recordType,
            workName = workName,
            reference = reference,
            cycleNumberText = cycleNumberText,
            cycleUnitText = cycleUnitText,
            status = status,
            comment = comment,
            attachments = attachments
        )
    }

    fun getRecord(recordId: Long): TaskWorkRecordItem? {
        return repository.getRecord(recordId)
    }

    fun updateRecordStatus(
        recordId: Long,
        status: TaskWorkRecordStatus
    ): Boolean {
        return repository.updateRecordStatus(
            recordId = recordId,
            status = status
        )
    }

    fun deleteRecord(recordId: Long): Boolean {
        return repository.deleteRecord(recordId)
    }
}
