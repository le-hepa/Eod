package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskWorkRecordDatabase
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class TaskWorkRecordDaoImpl(
    private val database: TaskWorkRecordDatabase
) : TaskWorkRecordDao {

    override fun allRecords(): List<TaskWorkRecordItem> {
        database.ensureLoadedForMutation()
        return database.records.value
    }

    override fun recordsForVessel(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            all.filter { it.vesselId == vesselId }.sortedNewestFirst()
        }
    }

    override fun recordsGroupedByDate(vesselId: Long): Flow<Map<LocalDate, List<TaskWorkRecordItem>>> {
        return database.records.map { all ->
            all.asSequence()
                .filter { it.vesselId == vesselId }
                .groupBy { it.recordDate }
                .mapValues { (_, items) -> items.sortedForDisplay() }
        }
    }

    override fun recordsForDate(vesselId: Long, recordDate: LocalDate): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            all.filter { it.vesselId == vesselId && it.recordDate == recordDate }
                .sortedForDisplay()
        }
    }

    override fun recordsForDateByType(
        vesselId: Long,
        recordDate: LocalDate,
        recordType: TaskWorkRecordType
    ): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            all.filter {
                it.vesselId == vesselId &&
                    it.recordDate == recordDate &&
                    it.recordType == recordType
            }.sortedForDisplay()
        }
    }

    override fun listRecordsForVessel(
        vesselId: Long,
        recordDate: LocalDate,
        includeAllDates: Boolean,
        query: String
    ): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            all.asSequence()
                .filter { it.vesselId == vesselId }
                .filter { includeAllDates || it.recordDate == recordDate }
                .filter {
                    query.isBlank() || it.workName.contains(query.trim(), ignoreCase = true)
                }
                .toList()
                .sortedForRecordList()
        }
    }

    override fun recentRecordsFor(
        vesselId: Long,
        recordType: TaskWorkRecordType,
        presetWorkId: Long?,
        workName: String
    ): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            val trimmedName = workName.trim()

            all.filter { record ->
                when (recordType) {
                    TaskWorkRecordType.REGULAR ->
                        presetWorkId != null &&
                            record.vesselId == vesselId &&
                            record.recordType == TaskWorkRecordType.REGULAR &&
                            record.presetWorkId == presetWorkId

                    TaskWorkRecordType.IRREGULAR ->
                        trimmedName.isNotBlank() &&
                            record.vesselId == vesselId &&
                            record.recordType == TaskWorkRecordType.IRREGULAR &&
                            record.workName == trimmedName
                }
            }.sortedNewestFirst()
        }
    }

    override fun latestRegularRecordForWork(vesselId: Long, presetWorkId: Long): TaskWorkRecordItem? {
        database.ensureLoadedForMutation()
        return database.records.value
            .asSequence()
            .filter {
                it.vesselId == vesselId &&
                    it.recordType == TaskWorkRecordType.REGULAR &&
                    it.presetWorkId == presetWorkId
            }
            .toList()
            .sortedNewestFirst()
            .firstOrNull()
    }

    override fun irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem> {
        database.ensureLoadedForMutation()
        return database.records.value.irregularAlarmCandidatesForVessel(vesselId)
    }

    override fun irregularAlarmCandidatesForVesselFlow(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return database.records.map { all ->
            all.irregularAlarmCandidatesForVessel(vesselId)
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
        database.ensureLoadedForMutation()
        val trimmedName = workName.trim()
        if (trimmedName.isBlank()) return null

        val finalRecordId = recordId?.takeIf { getRecord(it) != null } ?: database.nextId.getAndIncrement()
        val normalizedAttachments = attachments.map { attachment ->
            attachment.copy(
                id = if (attachment.id > 0L) attachment.id else database.nextAttachmentId.getAndIncrement(),
                recordId = finalRecordId,
                uri = attachment.uri.trim(),
                displayName = attachment.displayName.trim()
            )
        }.filter { it.uri.isNotBlank() }

        if (recordId != null && getRecord(recordId) != null) {
            database.records.update { current ->
                current.map { item ->
                    if (item.id == recordId) {
                        item.copy(
                            recordDate = recordDate,
                            presetWorkId = presetWorkId,
                            recordType = recordType,
                            workName = trimmedName,
                            reference = reference.trim(),
                            cycleNumberText = cycleNumberText.trim(),
                            cycleUnitText = cycleUnitText.trim(),
                            status = status,
                            comment = comment.trim(),
                            attachments = normalizedAttachments
                        )
                    } else {
                        item
                    }
                }
            }
            database.persist(database.records.value)
            return recordId
        }

        database.records.update { current ->
            current + TaskWorkRecordItem(
                id = finalRecordId,
                vesselId = vesselId,
                recordDate = recordDate,
                presetWorkId = presetWorkId,
                recordType = recordType,
                workName = trimmedName,
                reference = reference.trim(),
                cycleNumberText = cycleNumberText.trim(),
                cycleUnitText = cycleUnitText.trim(),
                status = status,
                comment = comment.trim(),
                attachments = normalizedAttachments
            )
        }
        database.persist(database.records.value)
        return finalRecordId
    }

    override fun getRecord(recordId: Long): TaskWorkRecordItem? {
        database.ensureLoadedForMutation()
        return database.records.value.firstOrNull { it.id == recordId }
    }

    override fun updateRecordStatus(
        recordId: Long,
        status: TaskWorkRecordStatus
    ): Boolean {
        database.ensureLoadedForMutation()
        var updated = false
        database.records.update { current ->
            current.map { item ->
                if (item.id == recordId) {
                    updated = true
                    item.copy(status = status)
                } else {
                    item
                }
            }
        }
        if (updated) database.persist(database.records.value)
        return updated
    }

    override fun deleteRecord(recordId: Long): Boolean {
        database.ensureLoadedForMutation()
        val removed = database.records.value.any { it.id == recordId }
        if (!removed) return false
        database.records.value = database.records.value.filterNot { it.id == recordId }
        database.persist(database.records.value)
        return true
    }

    override fun clearAll() {
        database.ensureLoadedForMutation()
        database.records.value = emptyList()
        database.nextId.set(1L)
        database.nextAttachmentId.set(1L)
        database.persist(emptyList())
    }

    private fun List<TaskWorkRecordItem>.sortedNewestFirst(): List<TaskWorkRecordItem> {
        return sortedWith(
            compareByDescending<TaskWorkRecordItem> { it.recordDate }
                .thenByDescending { it.id }
        )
    }

    private fun List<TaskWorkRecordItem>.sortedForDisplay(): List<TaskWorkRecordItem> {
        return sortedWith(
            compareBy<TaskWorkRecordItem> {
                if (it.status == TaskWorkRecordStatus.RegularDelayed || it.status == TaskWorkRecordStatus.NonRegularDelayed) 0 else 1
            }.thenByDescending { it.id }
        )
    }

    private fun List<TaskWorkRecordItem>.sortedForRecordList(): List<TaskWorkRecordItem> {
        return sortedWith(
            compareBy<TaskWorkRecordItem> {
                if (it.status == TaskWorkRecordStatus.RegularDelayed || it.status == TaskWorkRecordStatus.NonRegularDelayed) 0 else 1
            }.thenByDescending { it.recordDate }
                .thenByDescending { it.id }
        )
    }

    private fun List<TaskWorkRecordItem>.irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem> {
        return asSequence()
            .filter { it.vesselId == vesselId }
            .filter { it.recordType == TaskWorkRecordType.IRREGULAR }
            .filter { it.status != TaskWorkRecordStatus.NonRegularDone }
            .filter { it.workName.trim().isNotBlank() }
            .groupBy { it.workName.trim() }
            .values
            .mapNotNull { grouped ->
                grouped.sortedNewestFirst().firstOrNull()
            }
            .sortedBy { it.workName.lowercase() }
    }
}
