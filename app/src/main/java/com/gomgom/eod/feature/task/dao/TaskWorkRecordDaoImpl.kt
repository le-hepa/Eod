package com.gomgom.eod.feature.task.dao

import com.gomgom.eod.feature.task.db.TaskWorkRecordDatabase
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class TaskWorkRecordDaoImpl(
    private val database: TaskWorkRecordDatabase
) : TaskWorkRecordDao {
    private data class RecordIndex(
        val recordsById: Map<Long, TaskWorkRecordItem>,
        val recordsByVesselNewest: Map<Long, List<TaskWorkRecordItem>>,
        val groupedByVesselDate: Map<Long, Map<LocalDate, List<TaskWorkRecordItem>>>,
        val recordsByVesselDate: Map<Pair<Long, LocalDate>, List<TaskWorkRecordItem>>,
        val recordsByVesselDateType: Map<Triple<Long, LocalDate, TaskWorkRecordType>, List<TaskWorkRecordItem>>,
        val latestRegularByWork: Map<Pair<Long, Long>, TaskWorkRecordItem>,
        val recentRegularByWork: Map<Pair<Long, Long>, List<TaskWorkRecordItem>>,
        val recentIrregularByWork: Map<Pair<Long, String>, List<TaskWorkRecordItem>>,
        val irregularAlarmByVessel: Map<Long, List<TaskWorkRecordItem>>
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val recordIndex = MutableStateFlow(buildRecordIndex(emptyList()))

    init {
        scope.launch {
            database.records.collect { records ->
                recordIndex.value = buildRecordIndex(records)
            }
        }
    }

    override fun allRecords(): List<TaskWorkRecordItem> {
        database.ensureLoadedForMutation()
        return database.records.value
    }

    override fun recordsForVessel(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return recordIndex.map { indexed ->
            indexed.recordsByVesselNewest[vesselId].orEmpty()
        }
    }

    override fun recordsGroupedByDate(vesselId: Long): Flow<Map<LocalDate, List<TaskWorkRecordItem>>> {
        return recordIndex.map { indexed ->
            indexed.groupedByVesselDate[vesselId].orEmpty()
        }
    }

    override fun recordsForDate(vesselId: Long, recordDate: LocalDate): Flow<List<TaskWorkRecordItem>> {
        return recordIndex.map { indexed ->
            indexed.recordsByVesselDate[vesselId to recordDate].orEmpty()
        }
    }

    override fun recordsForDateByType(
        vesselId: Long,
        recordDate: LocalDate,
        recordType: TaskWorkRecordType
    ): Flow<List<TaskWorkRecordItem>> {
        return recordIndex.map { indexed ->
            indexed.recordsByVesselDateType[Triple(vesselId, recordDate, recordType)].orEmpty()
        }
    }

    override fun listRecordsForVessel(
        vesselId: Long,
        recordDate: LocalDate,
        includeAllDates: Boolean,
        query: String
    ): Flow<List<TaskWorkRecordItem>> {
        val trimmedQuery = query.trim()
        return recordIndex.map { indexed ->
            val base = if (includeAllDates) {
                indexed.recordsByVesselNewest[vesselId].orEmpty()
            } else {
                indexed.recordsByVesselDate[vesselId to recordDate].orEmpty()
            }

            base.asSequence()
                .filter {
                    trimmedQuery.isBlank() || it.workName.contains(trimmedQuery, ignoreCase = true)
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
        val trimmedName = workName.trim()
        return recordIndex.map { indexed ->
            when (recordType) {
                TaskWorkRecordType.REGULAR ->
                    presetWorkId?.let { indexed.recentRegularByWork[vesselId to it].orEmpty() }.orEmpty()

                TaskWorkRecordType.IRREGULAR ->
                    if (trimmedName.isBlank()) {
                        emptyList()
                    } else {
                        indexed.recentIrregularByWork[vesselId to trimmedName].orEmpty()
                    }
            }
        }
    }

    override fun latestRegularRecordForWork(vesselId: Long, presetWorkId: Long): TaskWorkRecordItem? {
        database.ensureLoadedForMutation()
        return recordIndex.value.latestRegularByWork[vesselId to presetWorkId]
    }

    override fun irregularAlarmCandidatesForVessel(vesselId: Long): List<TaskWorkRecordItem> {
        database.ensureLoadedForMutation()
        return recordIndex.value.irregularAlarmByVessel[vesselId].orEmpty()
    }

    override fun irregularAlarmCandidatesForVesselFlow(vesselId: Long): Flow<List<TaskWorkRecordItem>> {
        return recordIndex.map { indexed ->
            indexed.irregularAlarmByVessel[vesselId].orEmpty()
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

    private fun buildRecordIndex(records: List<TaskWorkRecordItem>): RecordIndex {
        val recordsById = records.associateBy { it.id }
        val recordsByVessel = records.groupBy { it.vesselId }
        val recordsByVesselNewest = recordsByVessel.mapValues { (_, items) ->
            items.sortedNewestFirst()
        }
        val groupedByVesselDate = recordsByVessel.mapValues { (_, items) ->
            items.groupBy { it.recordDate }
                .mapValues { (_, grouped) -> grouped.sortedForDisplay() }
        }
        val recordsByVesselDate = groupedByVesselDate.flatMap { (vesselId, grouped) ->
            grouped.map { (date, items) -> (vesselId to date) to items }
        }.toMap()
        val recordsByVesselDateType = recordsByVesselDate.flatMap { (key, items) ->
            items.groupBy { it.recordType }
                .map { (type, grouped) ->
                    Triple(key.first, key.second, type) to grouped.sortedForDisplay()
                }
        }.toMap()
        val regularRecords = records.asSequence()
            .filter { it.recordType == TaskWorkRecordType.REGULAR && it.presetWorkId != null }
            .toList()
        val latestRegularByWork = regularRecords.groupBy { it.vesselId to requireNotNull(it.presetWorkId) }
            .mapValues { (_, items) -> items.sortedNewestFirst().first() }
        val recentRegularByWork = regularRecords.groupBy { it.vesselId to requireNotNull(it.presetWorkId) }
            .mapValues { (_, items) -> items.sortedNewestFirst() }
        val recentIrregularByWork = records.asSequence()
            .filter { it.recordType == TaskWorkRecordType.IRREGULAR }
            .filter { it.workName.trim().isNotBlank() }
            .groupBy { it.vesselId to it.workName.trim() }
            .mapValues { (_, items) -> items.sortedNewestFirst() }
        val irregularAlarmByVessel = recordsByVessel.mapValues { (vesselId, items) ->
            items.irregularAlarmCandidatesForVessel(vesselId)
        }

        return RecordIndex(
            recordsById = recordsById,
            recordsByVesselNewest = recordsByVesselNewest,
            groupedByVesselDate = groupedByVesselDate,
            recordsByVesselDate = recordsByVesselDate,
            recordsByVesselDateType = recordsByVesselDateType,
            latestRegularByWork = latestRegularByWork,
            recentRegularByWork = recentRegularByWork,
            recentIrregularByWork = recentIrregularByWork,
            irregularAlarmByVessel = irregularAlarmByVessel
        )
    }
}
