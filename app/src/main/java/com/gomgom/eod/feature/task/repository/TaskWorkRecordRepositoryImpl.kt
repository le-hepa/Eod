package com.gomgom.eod.feature.task.repository

import android.util.Log
import com.gomgom.eod.feature.task.dao.TaskWorkRecordDao
import com.gomgom.eod.feature.task.dao.TaskWorkRecordDaoProvider
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskWorkRecordRepositoryImpl(
    private val dao: TaskWorkRecordDao = TaskWorkRecordDaoProvider.dao
) : TaskWorkRecordRepository {

    companion object {
        private const val TAG = "EOD_DB"
    }

    override fun allRecords(): List<TaskWorkRecordItem> {
        return runCatching { dao.allRecords() }
            .onFailure { Log.e(TAG, "allRecords failed", it) }
            .getOrDefault(emptyList())
    }

    override fun recordsForVessel(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return dao.recordsForVessel(vesselId)
    }

    override fun recordsGroupedByDate(vesselId: Long): Flow<Map<LocalDate, List<TaskWorkRecordItem>>> {
        return dao.recordsGroupedByDate(vesselId)
    }

    override fun recordsForDate(vesselId: Long, recordDate: LocalDate): Flow<List<TaskWorkRecordItem>> {
        return dao.recordsForDate(vesselId, recordDate)
    }

    override fun recordsForDateByType(
        vesselId: Long,
        recordDate: LocalDate,
        recordType: TaskWorkRecordType
    ): Flow<List<TaskWorkRecordItem>> {
        return dao.recordsForDateByType(
            vesselId = vesselId,
            recordDate = recordDate,
            recordType = recordType
        )
    }

    override fun listRecordsForVessel(
        vesselId: Long,
        recordDate: LocalDate,
        includeAllDates: Boolean,
        query: String
    ): Flow<List<TaskWorkRecordItem>> {
        return dao.listRecordsForVessel(
            vesselId = vesselId,
            recordDate = recordDate,
            includeAllDates = includeAllDates,
            query = query
        )
    }

    override fun recentRecordsFor(
        vesselId: Long,
        recordType: TaskWorkRecordType,
        presetWorkId: Long?,
        workName: String
    ): Flow<List<TaskWorkRecordItem>> {
        return dao.recentRecordsFor(
            vesselId = vesselId,
            recordType = recordType,
            presetWorkId = presetWorkId,
            workName = workName
        )
    }

    override fun latestRegularRecordForWork(vesselId: Long, presetWorkId: Long): TaskWorkRecordItem? {
        return dao.latestRegularRecordForWork(vesselId, presetWorkId)
    }

    override fun irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem> {
        return dao.irregularAlarmCandidatesForVessel(vesselId)
    }

    override fun irregularAlarmCandidatesForVesselFlow(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return dao.irregularAlarmCandidatesForVesselFlow(vesselId)
    }

    override fun buildAlarmVisibleItems(
        items: List<TaskAlarmDisplayItem>,
        filter: TaskAlarmDisplayFilter,
        sort: TaskAlarmDisplaySort
    ): List<TaskAlarmDisplayItem> {
        val filteredItems = items.filter { item ->
            when (filter) {
                TaskAlarmDisplayFilter.ALL -> true
                TaskAlarmDisplayFilter.REGULAR -> item.type == TaskAlarmDisplayType.REGULAR
                TaskAlarmDisplayFilter.IRREGULAR -> item.type == TaskAlarmDisplayType.IRREGULAR
            }
        }

        return when (sort) {
            TaskAlarmDisplaySort.BASIC,
            TaskAlarmDisplaySort.A_TO_Z -> filteredItems.sortedBy { it.name.lowercase() }

            TaskAlarmDisplaySort.Z_TO_A -> filteredItems.sortedByDescending { it.name.lowercase() }
            TaskAlarmDisplaySort.DAY -> filteredItems.sortedForCycleUnit(CycleUnit.DAY)
            TaskAlarmDisplaySort.WEEK -> filteredItems.sortedForCycleUnit(CycleUnit.WEEK)
            TaskAlarmDisplaySort.MONTH -> filteredItems.sortedForCycleUnit(CycleUnit.MONTH)
            TaskAlarmDisplaySort.YEAR -> filteredItems.sortedForCycleUnit(CycleUnit.YEAR)
        }
    }

    override fun saveRecord(
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
        Log.d(TAG, "saveRecord triggered: recordId=$recordId vesselId=$vesselId attachments=${attachments.size}")
        return runCatching {
            dao.saveRecord(
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
        }.onFailure {
            Log.e(TAG, "saveRecord failed: recordId=$recordId", it)
        }.getOrNull()
    }

    override fun getRecord(recordId: Long): TaskWorkRecordItem? {
        return runCatching { dao.getRecord(recordId) }
            .onFailure { Log.e(TAG, "getRecord failed: recordId=$recordId", it) }
            .getOrNull()
    }

    override fun updateRecordStatus(
        recordId: Long,
        status: TaskWorkRecordStatus
    ): Boolean {
        Log.d(TAG, "updateRecordStatus triggered: recordId=$recordId status=$status")
        return runCatching {
            dao.updateRecordStatus(
                recordId = recordId,
                status = status
            )
        }.onFailure {
            Log.e(TAG, "updateRecordStatus failed: recordId=$recordId", it)
        }.getOrDefault(false)
    }

    override fun deleteRecord(recordId: Long): Boolean {
        Log.d(TAG, "deleteRecord triggered: recordId=$recordId")
        return runCatching { dao.deleteRecord(recordId) }
            .onFailure { Log.e(TAG, "deleteRecord failed: recordId=$recordId", it) }
            .getOrDefault(false)
    }

    override fun clearAll() {
        Log.d(TAG, "clearAll triggered")
        runCatching { dao.clearAll() }
            .onFailure { Log.e(TAG, "clearAll failed", it) }
    }

    private fun List<TaskAlarmDisplayItem>.sortedForCycleUnit(priorityUnit: CycleUnit): List<TaskAlarmDisplayItem> {
        return sortedWith(
            compareBy<TaskAlarmDisplayItem>(
                { if (it.cycleUnit == priorityUnit) 0 else 1 },
                { cycleUnitOrder(it.cycleUnit) },
                { it.cycleNumber ?: Int.MAX_VALUE },
                { it.name.lowercase() }
            )
        )
    }

    private fun cycleUnitOrder(cycleUnit: CycleUnit?): Int {
        return when (cycleUnit) {
            CycleUnit.DAY -> 0
            CycleUnit.WEEK -> 1
            CycleUnit.MONTH -> 2
            CycleUnit.YEAR -> 3
            null -> 4
        }
    }
}
