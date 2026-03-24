package com.gomgom.eod.feature.task.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.gomgom.eod.EodApp
import com.gomgom.eod.feature.task.model.TaskAlarmSettings
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentType
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.viewmodel.TaskPresetGroupItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskTopVesselItem
import org.json.JSONArray
import java.time.LocalDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Entity(tableName = "task_preset_groups")
data class TaskPresetGroupEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val enabled: Boolean
)

@Entity(tableName = "task_preset_works")
data class TaskPresetWorkEntity(
    @PrimaryKey val id: Long,
    val presetId: Long,
    val name: String,
    val reference: String,
    val cycleNumber: Int,
    val cycleUnit: String,
    val alarmEnabled: Boolean
)

@Entity(tableName = "task_vessels")
data class TaskVesselEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val presetName: String,
    val enabled: Boolean
)

@Entity(tableName = "task_work_records")
data class TaskWorkRecordEntity(
    @PrimaryKey val id: Long,
    val vesselId: Long,
    val recordDate: String,
    val presetWorkId: Long?,
    val recordType: String,
    val workName: String,
    val reference: String,
    val cycleNumberText: String,
    val cycleUnitText: String,
    val status: String,
    val comment: String
)

@Entity(tableName = "task_work_record_attachments")
data class TaskWorkRecordAttachmentEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val type: String,
    val uri: String,
    val displayName: String
)

@Entity(tableName = "task_alarm_settings")
data class TaskAlarmSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val masterAlarmEnabled: Boolean,
    val alarmTime: String
)

@Entity(tableName = "task_regular_alarm_states")
data class TaskRegularAlarmStateEntity(
    @PrimaryKey val workId: Long,
    val enabled: Boolean
)

@Entity(tableName = "task_irregular_alarm_states")
data class TaskIrregularAlarmStateEntity(
    @PrimaryKey val key: String,
    val enabled: Boolean
)

@Dao
interface TaskRoomDao {
    @Query("SELECT * FROM task_preset_groups ORDER BY id")
    fun getPresetGroups(): List<TaskPresetGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertPresetGroups(items: List<TaskPresetGroupEntity>)

    @Query("DELETE FROM task_preset_groups")
    fun clearPresetGroups()

    @Query("SELECT * FROM task_preset_works ORDER BY id")
    fun getPresetWorks(): List<TaskPresetWorkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertPresetWorks(items: List<TaskPresetWorkEntity>)

    @Query("DELETE FROM task_preset_works")
    fun clearPresetWorks()

    @Query("SELECT * FROM task_vessels ORDER BY id")
    fun getVessels(): List<TaskVesselEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertVessels(items: List<TaskVesselEntity>)

    @Query("DELETE FROM task_vessels WHERE id IN (:ids)")
    fun deleteVesselsByIds(ids: List<Long>)

    @Query("DELETE FROM task_vessels")
    fun clearVessels()

    @Query("SELECT * FROM task_work_records ORDER BY id")
    fun getWorkRecords(): List<TaskWorkRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertWorkRecords(items: List<TaskWorkRecordEntity>)

    @Query("DELETE FROM task_work_records WHERE id IN (:ids)")
    fun deleteWorkRecordsByIds(ids: List<Long>)

    @Query("DELETE FROM task_work_records")
    fun clearWorkRecords()

    @Query("SELECT * FROM task_work_record_attachments ORDER BY id")
    fun getAttachments(): List<TaskWorkRecordAttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAttachments(items: List<TaskWorkRecordAttachmentEntity>)

    @Query("DELETE FROM task_work_record_attachments WHERE id IN (:ids)")
    fun deleteAttachmentsByIds(ids: List<Long>)

    @Query("DELETE FROM task_work_record_attachments")
    fun clearAttachments()

    @Query("SELECT * FROM task_alarm_settings LIMIT 1")
    fun getAlarmSettings(): TaskAlarmSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAlarmSettings(item: TaskAlarmSettingsEntity)

    @Query("SELECT * FROM task_regular_alarm_states")
    fun getRegularAlarmStates(): List<TaskRegularAlarmStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertRegularAlarmStates(items: List<TaskRegularAlarmStateEntity>)

    @Query("DELETE FROM task_regular_alarm_states")
    fun clearRegularAlarmStates()

    @Query("SELECT * FROM task_irregular_alarm_states")
    fun getIrregularAlarmStates(): List<TaskIrregularAlarmStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertIrregularAlarmStates(items: List<TaskIrregularAlarmStateEntity>)

    @Query("DELETE FROM task_irregular_alarm_states")
    fun clearIrregularAlarmStates()
}

@Database(
    entities = [
        TaskPresetGroupEntity::class,
        TaskPresetWorkEntity::class,
        TaskVesselEntity::class,
        TaskWorkRecordEntity::class,
        TaskWorkRecordAttachmentEntity::class,
        TaskAlarmSettingsEntity::class,
        TaskRegularAlarmStateEntity::class,
        TaskIrregularAlarmStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TaskRoomDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskRoomDao

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null
        private val migrationMutex = Mutex()
        @Volatile
        private var migrationDone = false

        fun getInstance(context: Context = EodApp.appContext): TaskRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    TaskRoomDatabase::class.java,
                    "eod_task_room.db"
                )
                    .build()
                    .also { db -> INSTANCE = db }
            }
        }

        suspend fun ensureMigrated(context: Context = EodApp.appContext) {
            if (migrationDone) return
            migrationMutex.withLock {
                if (migrationDone) return
                TaskRoomMigrator.migrateIfNeeded(context, getInstance(context).taskDao())
                migrationDone = true
            }
        }
    }
}

object TaskRoomMigrator {
    private const val PREFS_NAME = "eod_task_room_migration"
    private const val KEY_DONE = "done"

    fun migrateIfNeeded(context: Context, dao: TaskRoomDao) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONE, false)) return

        val presetPrefs = context.getSharedPreferences("eod_task_preset_store", Context.MODE_PRIVATE)
        val presetWorkPrefs = context.getSharedPreferences("eod_task_preset_work_store", Context.MODE_PRIVATE)
        val topPrefs = context.getSharedPreferences("eod_task_top_store", Context.MODE_PRIVATE)
        val workRecordPrefs = context.getSharedPreferences("eod_task_work_records", Context.MODE_PRIVATE)
        val alarmPrefs = context.getSharedPreferences("eod_task_alarm_settings", Context.MODE_PRIVATE)

        dao.clearPresetGroups()
        dao.clearPresetWorks()
        dao.clearVessels()
        dao.clearAttachments()
        dao.clearWorkRecords()
        dao.clearRegularAlarmStates()
        dao.clearIrregularAlarmStates()

        dao.upsertPresetGroups(loadPresetGroupsFromPrefs(presetPrefs.getString("preset_groups", null).orEmpty()))
        dao.upsertPresetWorks(loadPresetWorksFromPrefs(presetWorkPrefs.getString("works", null).orEmpty()))
        dao.upsertVessels(loadVesselsFromPrefs(topPrefs.getString("vessels", null).orEmpty()))

        val recordPair = loadWorkRecordsFromPrefs(workRecordPrefs.getString("records", null).orEmpty())
        dao.upsertWorkRecords(recordPair.first)
        dao.upsertAttachments(recordPair.second)

        dao.upsertAlarmSettings(
            TaskAlarmSettingsEntity(
                masterAlarmEnabled = alarmPrefs.getBoolean("master_enabled", false),
                alarmTime = alarmPrefs.getString("alarm_time", "08:00") ?: "08:00"
            )
        )
        dao.upsertRegularAlarmStates(
            TaskAlarmSettingsDatabase.decodeLongBooleanMap(
                alarmPrefs.getStringSet("regular_states", emptySet()).orEmpty()
            ).map { TaskRegularAlarmStateEntity(workId = it.key, enabled = it.value) }
        )
        dao.upsertIrregularAlarmStates(
            TaskAlarmSettingsDatabase.decodeStringBooleanMap(
                alarmPrefs.getStringSet("irregular_states", emptySet()).orEmpty()
            ).map { TaskIrregularAlarmStateEntity(key = it.key, enabled = it.value) }
        )

        prefs.edit().putBoolean(KEY_DONE, true).apply()
    }

    private fun loadPresetGroupsFromPrefs(raw: String): List<TaskPresetGroupEntity> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        TaskPresetGroupEntity(
                            id = item.optLong("id"),
                            name = item.optString("name"),
                            enabled = item.optBoolean("enabled", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun loadPresetWorksFromPrefs(raw: String): List<TaskPresetWorkEntity> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        TaskPresetWorkEntity(
                            id = item.optLong("id"),
                            presetId = item.optLong("presetId"),
                            name = item.optString("name"),
                            reference = item.optString("reference"),
                            cycleNumber = item.optInt("cycleNumber"),
                            cycleUnit = item.optString("cycleUnit"),
                            alarmEnabled = item.optBoolean("alarmEnabled", true)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun loadVesselsFromPrefs(raw: String): List<TaskVesselEntity> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        TaskVesselEntity(
                            id = item.optLong("id"),
                            name = item.optString("name"),
                            presetName = item.optString("presetName"),
                            enabled = item.optBoolean("enabled", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun loadWorkRecordsFromPrefs(raw: String): Pair<List<TaskWorkRecordEntity>, List<TaskWorkRecordAttachmentEntity>> {
        if (raw.isBlank()) return emptyList<TaskWorkRecordEntity>() to emptyList()
        return runCatching {
            val array = JSONArray(raw)
            val records = mutableListOf<TaskWorkRecordEntity>()
            val attachments = mutableListOf<TaskWorkRecordAttachmentEntity>()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val recordId = item.optLong("id")
                records += TaskWorkRecordEntity(
                    id = recordId,
                    vesselId = item.optLong("vesselId"),
                    recordDate = item.optString("recordDate"),
                    presetWorkId = item.takeUnless { it.isNull("presetWorkId") }?.optLong("presetWorkId"),
                    recordType = item.optString("recordType"),
                    workName = item.optString("workName"),
                    reference = item.optString("reference"),
                    cycleNumberText = item.optString("cycleNumberText"),
                    cycleUnitText = item.optString("cycleUnitText"),
                    status = item.optString("status"),
                    comment = item.optString("comment")
                )
                val attachmentArray = item.optJSONArray("attachments") ?: JSONArray()
                for (j in 0 until attachmentArray.length()) {
                    val attachment = attachmentArray.optJSONObject(j) ?: continue
                    attachments += TaskWorkRecordAttachmentEntity(
                        id = attachment.optLong("id"),
                        recordId = attachment.optLong("recordId"),
                        type = attachment.optString("type"),
                        uri = attachment.optString("uri"),
                        displayName = attachment.optString("displayName")
                    )
                }
            }
            records to attachments
        }.getOrDefault(emptyList<TaskWorkRecordEntity>() to emptyList())
    }
}

fun TaskPresetGroupEntity.toItem(works: List<TaskPresetWorkItem>): TaskPresetGroupItem =
    TaskPresetGroupItem(id = id, name = name, enabled = enabled, works = works)

fun TaskPresetWorkEntity.toItem(): TaskPresetWorkItem =
    TaskPresetWorkItem(
        id = id,
        presetId = presetId,
        name = name,
        reference = reference,
        cycleNumber = cycleNumber,
        cycleUnit = CycleUnit.valueOf(cycleUnit),
        alarmEnabled = alarmEnabled
    )

fun TaskTopVesselItem.toEntity(): TaskVesselEntity =
    TaskVesselEntity(id = id, name = name, presetName = presetName, enabled = enabled)

fun TaskVesselEntity.toItem(): TaskTopVesselItem =
    TaskTopVesselItem(id = id, name = name, presetName = presetName, enabled = enabled)

fun TaskWorkRecordEntity.toItem(attachments: List<TaskWorkRecordAttachmentItem>): TaskWorkRecordItem =
    TaskWorkRecordItem(
        id = id,
        vesselId = vesselId,
        recordDate = LocalDate.parse(recordDate),
        presetWorkId = presetWorkId,
        recordType = TaskWorkRecordType.valueOf(recordType),
        workName = workName,
        reference = reference,
        cycleNumberText = cycleNumberText,
        cycleUnitText = cycleUnitText,
        status = TaskWorkRecordStatus.valueOf(status),
        comment = comment,
        attachments = attachments
    )

fun TaskWorkRecordItem.toEntity(): TaskWorkRecordEntity =
    TaskWorkRecordEntity(
        id = id,
        vesselId = vesselId,
        recordDate = recordDate.toString(),
        presetWorkId = presetWorkId,
        recordType = recordType.name,
        workName = workName,
        reference = reference,
        cycleNumberText = cycleNumberText,
        cycleUnitText = cycleUnitText,
        status = status.name,
        comment = comment
    )

fun TaskWorkRecordAttachmentItem.toEntity(): TaskWorkRecordAttachmentEntity =
    TaskWorkRecordAttachmentEntity(
        id = id,
        recordId = recordId,
        type = type.name,
        uri = uri,
        displayName = displayName
    )

fun TaskWorkRecordAttachmentEntity.toItem(): TaskWorkRecordAttachmentItem =
    TaskWorkRecordAttachmentItem(
        id = id,
        recordId = recordId,
        type = TaskWorkRecordAttachmentType.valueOf(type),
        uri = uri,
        displayName = displayName
    )

fun TaskAlarmSettingsEntity.toModel(
    regularStates: Map<Long, Boolean>,
    irregularStates: Map<String, Boolean>
): TaskAlarmSettings =
    TaskAlarmSettings(
        masterAlarmEnabled = masterAlarmEnabled,
        alarmTime = alarmTime,
        regularWorkAlarmStates = regularStates,
        irregularWorkAlarmStates = irregularStates
    )
