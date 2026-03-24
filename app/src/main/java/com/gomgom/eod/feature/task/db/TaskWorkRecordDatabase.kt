package com.gomgom.eod.feature.task.db

import android.util.Log
import androidx.room.withTransaction
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

class TaskWorkRecordDatabase {
    companion object {
        private const val TAG = "EOD_DB"
    }

    private val roomDao by lazy { TaskRoomDatabase.getInstance().taskDao() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var loaded = false
    @Volatile
    private var preloadJob: Job? = null
    private val persistMutex = Mutex()
    val nextId: AtomicLong = AtomicLong(1L)
    val nextAttachmentId: AtomicLong = AtomicLong(1L)
    val records: MutableStateFlow<List<TaskWorkRecordItem>> = MutableStateFlow(emptyList())

    init {
        startPreload()
    }

    fun persist(records: List<TaskWorkRecordItem>) {
        scope.launch {
            try {
                Log.d(TAG, "persist work records count=${records.size}")
                persistMutex.withLock {
                    TaskRoomDatabase.ensureMigrated()
                    val roomDatabase = TaskRoomDatabase.getInstance()
                    val existingRecordEntities = roomDao.getWorkRecords()
                    val existingAttachmentEntities = roomDao.getAttachments()
                    val newRecordEntities = records.map { it.toEntity() }
                    val newAttachmentEntities = records.flatMap { it.attachments }.map { it.toEntity() }

                    val existingRecordById = existingRecordEntities.associateBy { it.id }
                    val newRecordById = newRecordEntities.associateBy { it.id }
                    val recordsToUpsert = newRecordEntities.filter { entity ->
                        existingRecordById[entity.id]?.isSameContentAs(entity) != true
                    }
                    val recordIdsToDelete = existingRecordById.keys
                        .filterNot(newRecordById::containsKey)

                    val existingAttachmentById = existingAttachmentEntities.associateBy { it.id }
                    val newAttachmentById = newAttachmentEntities.associateBy { it.id }
                    val attachmentsToUpsert = newAttachmentEntities.filter { entity ->
                        existingAttachmentById[entity.id]?.isSameContentAs(entity) != true
                    }
                    val attachmentIdsToDelete = existingAttachmentById.keys
                        .filterNot(newAttachmentById::containsKey)

                    roomDatabase.withTransaction {
                        if (attachmentIdsToDelete.isNotEmpty()) {
                            roomDao.deleteAttachmentsByIds(attachmentIdsToDelete)
                        }
                        if (recordIdsToDelete.isNotEmpty()) {
                            roomDao.deleteWorkRecordsByIds(recordIdsToDelete)
                        }
                        if (recordsToUpsert.isNotEmpty()) {
                            roomDao.upsertWorkRecords(recordsToUpsert)
                        }
                        if (attachmentsToUpsert.isNotEmpty()) {
                            roomDao.upsertAttachments(attachmentsToUpsert)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "persist work records failed", e)
            }
        }
    }

    private fun startPreload() {
        if (loaded) return
        val runningJob = preloadJob
        if (runningJob != null && runningJob.isActive) return
        preloadJob = scope.launch {
            try {
                val loadedRecords = loadRecords()
                nextId.set((loadedRecords.maxOfOrNull { it.id } ?: 0L) + 1L)
                nextAttachmentId.set(
                    (loadedRecords.flatMap { it.attachments }.maxOfOrNull { it.id } ?: 0L) + 1L
                )
                records.value = loadedRecords
                loaded = true
            } catch (e: Exception) {
                Log.e(TAG, "preload work records failed", e)
            }
        }
    }

    private suspend fun loadRecords(): List<TaskWorkRecordItem> {
        TaskRoomDatabase.ensureMigrated()
        val attachmentsByRecordId = roomDao.getAttachments()
            .map { it.toItem() }
            .groupBy { it.recordId }
        return roomDao.getWorkRecords()
            .map { entity -> entity.toItem(attachmentsByRecordId[entity.id].orEmpty()) }
    }

    fun ensureLoadedForMutation() {
        startPreload()
    }
}

private fun TaskWorkRecordEntity.isSameContentAs(other: TaskWorkRecordEntity): Boolean {
    return vesselId == other.vesselId &&
        recordDate == other.recordDate &&
        presetWorkId == other.presetWorkId &&
        recordType == other.recordType &&
        workName == other.workName &&
        reference == other.reference &&
        cycleNumberText == other.cycleNumberText &&
        cycleUnitText == other.cycleUnitText &&
        status == other.status &&
        comment == other.comment
}

private fun TaskWorkRecordAttachmentEntity.isSameContentAs(other: TaskWorkRecordAttachmentEntity): Boolean {
    return recordId == other.recordId &&
        type == other.type &&
        uri == other.uri &&
        displayName == other.displayName
}
