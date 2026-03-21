package com.gomgom.eod

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.core.navigation.EodNavGraph
import com.gomgom.eod.core.navigation.TaskAlarmNavigationBridge
import com.gomgom.eod.core.navigation.TaskAlarmNavigationTarget
import com.gomgom.eod.feature.task.alarm.TaskAlarmReceiver
import com.gomgom.eod.feature.task.alarm.TaskAlarmScheduler
import com.gomgom.eod.ui.theme.EodTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLanguageManager.ensureDefaultLanguage(this)
        super.onCreate(savedInstanceState)
        handleAlarmIntent(intent)
        TaskAlarmScheduler.syncAll(this)
        enableEdgeToEdge()

        setContent {
            EodTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val activity = LocalContext.current as? Activity
                    EodNavGraph(onExitApp = { activity?.finish() })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        val vesselId = intent?.getLongExtra(TaskAlarmReceiver.EXTRA_VESSEL_ID, -1L) ?: -1L
        val targetDateRaw = intent?.getStringExtra(TaskAlarmReceiver.EXTRA_TARGET_DATE).orEmpty()
        if (vesselId <= 0L || targetDateRaw.isBlank()) return

        val targetDate = runCatching { LocalDate.parse(targetDateRaw) }.getOrNull() ?: return
        val recordId = intent?.getLongExtra(TaskAlarmReceiver.EXTRA_RECORD_ID, -1L)
            ?.takeIf { it > 0L }

        TaskAlarmNavigationBridge.deliver(
            TaskAlarmNavigationTarget(
                vesselId = vesselId,
                targetDate = targetDate,
                targetRecordId = recordId
            )
        )
    }
}
