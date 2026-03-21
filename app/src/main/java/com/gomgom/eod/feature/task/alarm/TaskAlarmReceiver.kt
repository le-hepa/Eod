package com.gomgom.eod.feature.task.alarm

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gomgom.eod.MainActivity
import com.gomgom.eod.R
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val vesselId = intent.getLongExtra(EXTRA_VESSEL_ID, 0L)
        val vesselName = intent.getStringExtra(EXTRA_VESSEL_NAME).orEmpty()
        val vesselPresetName = intent.getStringExtra(EXTRA_VESSEL_PRESET_NAME).orEmpty()
        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, 0L)
        val targetDate = intent.getStringExtra(EXTRA_TARGET_DATE).orEmpty()
        val workName = intent.getStringExtra(EXTRA_WORK_NAME).orEmpty()
        val statusName = intent.getStringExtra(EXTRA_STATUS_NAME).orEmpty()
        val statusLabel = runCatching { TaskWorkRecordStatus.valueOf(statusName) }
            .getOrNull()
            ?.toNotificationStatusLabel(context)
            .orEmpty()

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_VESSEL_ID, vesselId)
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_TARGET_DATE, targetDate)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            "task_alarm_open_${recordId}_$targetDate".hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (vesselPresetName.isBlank()) vesselName else "$vesselName / $vesselPresetName"
        val body = "$workName - $statusLabel"

        val notification = NotificationCompat.Builder(context, TaskAlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(workName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify("task_alarm_$recordId$targetDate".hashCode(), notification)
    }

    companion object {
        const val EXTRA_VESSEL_ID = "task_alarm_vessel_id"
        const val EXTRA_RECORD_ID = "task_alarm_record_id"
        const val EXTRA_TARGET_DATE = "task_alarm_target_date"
        const val EXTRA_VESSEL_NAME = "task_alarm_vessel_name"
        const val EXTRA_VESSEL_PRESET_NAME = "task_alarm_vessel_preset_name"
        const val EXTRA_WORK_NAME = "task_alarm_work_name"
        const val EXTRA_STATUS_NAME = "task_alarm_status_name"
    }
}

private fun TaskWorkRecordStatus.toNotificationStatusLabel(context: Context): String {
    return when (this) {
        TaskWorkRecordStatus.RegularPlanned,
        TaskWorkRecordStatus.NonRegularPlanned -> context.getString(R.string.badge_status_planned)
        TaskWorkRecordStatus.RegularDelayed,
        TaskWorkRecordStatus.NonRegularDelayed -> context.getString(R.string.badge_status_delayed)
        TaskWorkRecordStatus.RegularDone,
        TaskWorkRecordStatus.NonRegularDone -> context.getString(R.string.badge_status_completed)
    }
}
