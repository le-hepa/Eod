package com.gomgom.eod.feature.task.db

import android.util.Log
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val persistMutex = Mutex()
    val nextId: AtomicLong = AtomicLong(1L)
    val nextAttachmentId: AtomicLong = AtomicLong(1L)
    val records: MutableStateFlow<List<TaskWorkRecordItem>> = MutableStateFlow(emptyList())

    init {
        scope.launch {
            val loadedRecords = loadRecords()
            nextId.set((loadedRecords.maxOfOrNull { it.id } ?: 0L) + 1L)
            nextAttachmentId.set(
                (loadedRecords.flatMap { it.attachments }.maxOfOrNull { it.id } ?: 0L) + 1L
            )
            records.value = loadedRecords
            loaded = true
        }
    }

    fun persist(records: List<TaskWorkRecordItem>) {
        scope.launch {
            try {
                Log.d(TAG, "persist work records count=${records.size}")
                persistMutex.withLock {
                    TaskRoomDatabase.ensureMigrated()
                    roomDao.clearAttachments()
                    roomDao.clearWorkRecords()
                    roomDao.upsertWorkRecords(records.map { it.toEntity() })
                    roomDao.upsertAttachments(records.flatMap { it.attachments }.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "persist work records failed", e)
            }
        }
    }

    private fun loadRecords(): List<TaskWorkRecordItem> {
        val attachmentsByRecordId = runBlocking(Dispatchers.IO) {
            TaskRoomDatabase.ensureMigrated()
            roomDao.getAttachments()
        }
            .map { it.toItem() }
            .groupBy { it.recordId }
        return runBlocking(Dispatchers.IO) {
            TaskRoomDatabase.ensureMigrated()
            roomDao.getWorkRecords()
        }
            .map { entity -> entity.toItem(attachmentsByRecordId[entity.id].orEmpty()) }
    }

    fun ensureLoadedForMutation() {
        if (loaded) return
        try {
            val loadedRecords = loadRecords()
            nextId.set((loadedRecords.maxOfOrNull { it.id } ?: 0L) + 1L)
            nextAttachmentId.set(
                (loadedRecords.flatMap { it.attachments }.maxOfOrNull { it.id } ?: 0L) + 1L
            )
            records.value = loadedRecords
            loaded = true
        } catch (e: Exception) {
            Log.e(TAG, "ensureLoadedForMutation failed", e)
        }
    }
}
