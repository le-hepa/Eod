package com.gomgom.eod.feature.task.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.gomgom.eod.R
import com.gomgom.eod.feature.task.repository.TaskAlarmSettingsRepositoryProvider
import com.gomgom.eod.feature.task.repository.TaskTopRepositoryProvider
import com.gomgom.eod.feature.task.repository.TaskWorkRecordRepositoryProvider
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkStateStore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object TaskAlarmScheduler {
    const val CHANNEL_ID = "task_work_alarm_channel"
    private const val PREFS_NAME = "eod_task_alarm_runtime"
    private const val KEY_SCHEDULED_KEYS = "scheduled_keys"
    private const val TAG = "EOD_ALARM"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncAll(context: Context) {
        scope.launch {
            syncAllNow(context.applicationContext)
        }
    }

    fun syncForVessel(context: Context, vesselId: Long) {
        scope.launch {
            runCatching {
                syncScopedNow(
                    context = context.applicationContext,
                    vesselId = vesselId
                )
            }.getOrElse {
                Log.e(TAG, "syncForVessel fallback to syncAll: vesselId=$vesselId", it)
                syncAllNow(context.applicationContext)
            }
        }
    }

    fun syncForRegularWork(context: Context, vesselId: Long, workId: Long) {
        scope.launch {
            runCatching {
                syncScopedNow(
                    context = context.applicationContext,
                    vesselId = vesselId,
                    regularWorkId = workId
                )
            }.getOrElse {
                Log.e(TAG, "syncForRegularWork fallback to syncAll: vesselId=$vesselId workId=$workId", it)
                syncAllNow(context.applicationContext)
            }
        }
    }

    fun syncForIrregularWork(context: Context, vesselId: Long, workName: String) {
        scope.launch {
            runCatching {
                syncScopedNow(
                    context = context.applicationContext,
                    vesselId = vesselId,
                    irregularWorkName = workName.trim()
                )
            }.getOrElse {
                Log.e(TAG, "syncForIrregularWork fallback to syncAll: vesselId=$vesselId workName=$workName", it)
                syncAllNow(context.applicationContext)
            }
        }
    }

    fun cancelAll(context: Context) {
        scope.launch {
            cancelAllNow(context.applicationContext)
        }
    }

    private fun syncAllNow(context: Context) {
        syncScopedNow(context)
    }

    private fun syncScopedNow(
        context: Context,
        vesselId: Long? = null,
        regularWorkId: Long? = null,
        irregularWorkName: String? = null
    ) {
        try {
            ensureNotificationChannel(context)
            val alarmManager = context.getSystemService<AlarmManager>() ?: return
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val previousKeys = prefs.getStringSet(KEY_SCHEDULED_KEYS, emptySet()).orEmpty()

            val topState = TaskTopRepositoryProvider.repository.uiState.value
            val alarmSettings = TaskAlarmSettingsRepositoryProvider.repository.settings.value
            if (!alarmSettings.masterAlarmEnabled) {
                Log.d(TAG, "sync skipped: master alarm off")
                previousKeys.forEach { key ->
                    cancelPendingIntent(context, alarmManager, key)
                }
                prefs.edit().putStringSet(KEY_SCHEDULED_KEYS, emptySet()).apply()
                return
            }

            val activePreset = TaskPresetStateStore.snapshot().firstOrNull { it.enabled }
            val works = TaskPresetWorkStateStore.snapshot()
            val vessels = topState.vesselItems.filter { it.enabled }
                .filter { vesselId == null || it.id == vesselId }
            val activeKeys = linkedSetOf<String>()
            val commonTime = parseAlarmTime(alarmSettings.alarmTime) ?: LocalTime.of(8, 0)
            val allRecordsById = TaskWorkRecordRepositoryProvider.repository
                .allRecords()
                .associateBy { it.id }

            vessels.forEach { vessel ->
                works.filter { it.presetId == activePreset?.id }
                    .filter { regularWorkId == null || it.id == regularWorkId }
                    .forEach { work ->
                    val regularEnabled = TaskAlarmSettingsRepositoryProvider.repository
                        .isRegularWorkAlarmEnabled(work.id, work.alarmEnabled)
                    if (!regularEnabled) return@forEach

                    val latestRecord = TaskWorkRecordRepositoryProvider.repository
                        .latestRegularRecordForWork(vessel.id, work.id)
                        ?: return@forEach

                    if (latestRecord.isCompleted() || isExpired(latestRecord.recordDate, work.cycleUnit)) {
                        return@forEach
                    }

                    val dueDate = latestRecord.recordDate.plusCycle(work.cycleNumber, work.cycleUnit)
                    val latestReminderDate = reminderDatesForRegular(dueDate, work.cycleUnit).maxOrNull()
                        ?: return@forEach
                    try {
                        scheduleIfNeeded(
                            context = context,
                            alarmManager = alarmManager,
                            vesselId = vessel.id,
                            vesselName = vessel.name,
                            vesselPresetName = vessel.presetName,
                            recordId = latestRecord.id,
                            workName = work.name,
                            statusName = latestRecord.status.name,
                            targetDate = dueDate,
                            triggerDate = latestReminderDate,
                            triggerTime = commonTime,
                            keyPrefix = "regular_${work.id}_${latestRecord.id}_$latestReminderDate",
                            scheduledKeys = activeKeys
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "regular schedule failed recordId=${latestRecord.id}", e)
                    }
                }

                TaskWorkRecordRepositoryProvider.repository
                    .irregularAlarmCandidatesForVessel(vessel.id)
                    .filter { irregularWorkName == null || it.workName.trim() == irregularWorkName }
                    .groupBy { it.workName.trim() }
                    .values
                    .mapNotNull { candidates ->
                        candidates.maxWithOrNull(
                            compareBy<TaskWorkRecordItem>({ it.recordDate }, { it.id })
                        )
                    }
                    .forEach { record ->
                        val irregularEnabled = TaskAlarmSettingsRepositoryProvider.repository
                            .isIrregularWorkAlarmEnabled(vessel.id, record.workName, true)
                        if (!irregularEnabled) return@forEach

                        if (record.isCompleted()) {
                            return@forEach
                        }

                        try {
                            scheduleIfNeeded(
                                context = context,
                                alarmManager = alarmManager,
                                vesselId = vessel.id,
                                vesselName = vessel.name,
                                vesselPresetName = vessel.presetName,
                                recordId = record.id,
                                workName = record.workName,
                                statusName = record.status.name,
                                targetDate = record.recordDate,
                                triggerDate = record.recordDate,
                                triggerTime = commonTime,
                                keyPrefix = "irregular_${vessel.id}_${record.workName.trim()}_${record.id}",
                                scheduledKeys = activeKeys
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "irregular schedule failed recordId=${record.id}", e)
                        }
                    }
            }

            val scopedPreviousKeys = previousKeys.filterTo(linkedSetOf()) { key ->
                key.matchesScope(
                    allRecordsById = allRecordsById,
                    vesselId = vesselId,
                    regularWorkId = regularWorkId,
                    irregularWorkName = irregularWorkName
                )
            }
            val toCancel = scopedPreviousKeys - activeKeys
            toCancel.forEach { key ->
                cancelPendingIntent(context, alarmManager, key)
            }

            Log.d(TAG, "active count = ${activeKeys.size}")
            Log.d(TAG, "cancel count = ${toCancel.size}")
            val mergedKeys = (previousKeys - scopedPreviousKeys) + activeKeys
            prefs.edit().putStringSet(KEY_SCHEDULED_KEYS, mergedKeys).apply()
        } catch (e: Exception) {
            Log.e(TAG, "syncScopedNow failed", e)
            throw e
        }
    }

    private fun cancelAllNow(context: Context) {
        try {
            val alarmManager = context.getSystemService<AlarmManager>() ?: return
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getStringSet(KEY_SCHEDULED_KEYS, emptySet()).orEmpty().forEach { key ->
                cancelPendingIntent(context, alarmManager, key)
            }
            prefs.edit().putStringSet(KEY_SCHEDULED_KEYS, emptySet()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "cancelAllNow failed", e)
        }
    }

    private fun scheduleIfNeeded(
        context: Context,
        alarmManager: AlarmManager,
        vesselId: Long,
        vesselName: String,
        vesselPresetName: String,
        recordId: Long,
        workName: String,
        statusName: String,
        targetDate: LocalDate,
        triggerDate: LocalDate,
        triggerTime: LocalTime,
        keyPrefix: String,
        scheduledKeys: MutableSet<String>
    ) {
        val triggerDateTime = LocalDateTime.of(triggerDate, triggerTime)
        if (triggerDateTime.isBefore(LocalDateTime.now())) return

        val triggerMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val key = "$keyPrefix@${triggerDate}_$triggerTime"
        val requestCode = key.hashCode()
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_VESSEL_ID, vesselId)
            putExtra(TaskAlarmReceiver.EXTRA_VESSEL_NAME, vesselName)
            putExtra(TaskAlarmReceiver.EXTRA_VESSEL_PRESET_NAME, vesselPresetName)
            putExtra(TaskAlarmReceiver.EXTRA_RECORD_ID, recordId)
            putExtra(TaskAlarmReceiver.EXTRA_TARGET_DATE, targetDate.toString())
            putExtra(TaskAlarmReceiver.EXTRA_WORK_NAME, workName)
            putExtra(TaskAlarmReceiver.EXTRA_STATUS_NAME, statusName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }

        scheduledKeys += key
    }

    private fun cancelPendingIntent(context: Context, alarmManager: AlarmManager, key: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            key.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun parseAlarmTime(raw: String): LocalTime? {
        val parts = raw.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return if (hour in 0..23 && minute in 0..59) LocalTime.of(hour, minute) else null
    }

    private fun reminderDatesForRegular(dueDate: LocalDate, cycleUnit: CycleUnit): List<LocalDate> {
        return when (cycleUnit) {
            CycleUnit.DAY -> listOf(dueDate)
            CycleUnit.WEEK -> listOf(dueDate.minusDays(1), dueDate).distinct()
            CycleUnit.MONTH -> listOf(dueDate.minusDays(5), dueDate).distinct()
            CycleUnit.YEAR -> listOf(dueDate.minusDays(10), dueDate).distinct()
        }
    }

    private fun LocalDate.plusCycle(cycleNumber: Int, cycleUnit: CycleUnit): LocalDate {
        return when (cycleUnit) {
            CycleUnit.DAY -> plusDays(cycleNumber.toLong())
            CycleUnit.WEEK -> plusWeeks(cycleNumber.toLong())
            CycleUnit.MONTH -> plusMonths(cycleNumber.toLong())
            CycleUnit.YEAR -> plusYears(cycleNumber.toLong())
        }
    }

    private fun TaskWorkRecordItem.isCompleted(): Boolean {
        return when (status) {
            TaskWorkRecordStatus.RegularDone,
            TaskWorkRecordStatus.NonRegularDone -> true

            else -> false
        }
    }

    private fun isExpired(recordDate: LocalDate, cycleUnit: CycleUnit): Boolean {
        val today = LocalDate.now()
        val ageDays = java.time.temporal.ChronoUnit.DAYS.between(recordDate, today)
        return when (cycleUnit) {
            CycleUnit.DAY -> ageDays > 7
            CycleUnit.WEEK -> ageDays > 14
            CycleUnit.MONTH -> ageDays > 60
            CycleUnit.YEAR -> false
        }
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.task_alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.task_alarm_channel_description)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    private fun String.matchesScope(
        allRecordsById: Map<Long, TaskWorkRecordItem>,
        vesselId: Long?,
        regularWorkId: Long?,
        irregularWorkName: String?
    ): Boolean {
        if (vesselId == null && regularWorkId == null && irregularWorkName == null) return true

        return when {
            startsWith("irregular_") -> {
                val parts = substringBefore("@").split("_")
                if (parts.size < 4) return false
                val keyVesselId = parts[1].toLongOrNull() ?: return false
                val keyRecordId = parts.last().toLongOrNull()
                val keyWorkName = parts.drop(2).dropLast(1).joinToString("_")
                (vesselId == null || keyVesselId == vesselId) &&
                    (irregularWorkName == null || keyWorkName == irregularWorkName) &&
                    (regularWorkId == null || false) &&
                    (keyRecordId == null || allRecordsById[keyRecordId]?.vesselId == null || vesselId == null || allRecordsById[keyRecordId]?.vesselId == vesselId)
            }

            startsWith("regular_") -> {
                val parts = substringBefore("@").split("_")
                if (parts.size < 4) return false
                val keyWorkId = parts[1].toLongOrNull() ?: return false
                val keyRecordId = parts[2].toLongOrNull() ?: return false
                val record = allRecordsById[keyRecordId]
                (regularWorkId == null || keyWorkId == regularWorkId) &&
                    (vesselId == null || record?.vesselId == vesselId)
            }

            else -> false
        }
    }
}
