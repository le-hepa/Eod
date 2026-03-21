package com.gomgom.eod.feature.task.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.task.alarm.TaskAlarmScheduler
import com.gomgom.eod.feature.task.screens.TaskTopScreen
import com.gomgom.eod.feature.task.screens.TaskVesselAddDialog
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel

@Composable
fun TaskToolWrapper(
    onBackClick: () -> Unit,
    onPresetClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onVesselClick: (Long) -> Unit,
    onAddVesselClick: () -> Unit,
    onDataManageClick: () -> Unit,
    onGuideClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TaskTopViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var vesselAddVisible by remember { mutableStateOf(false) }

    if (vesselAddVisible) {
        TaskVesselAddDialog(
            onDismiss = { vesselAddVisible = false },
            onGoPresetClick = {
                vesselAddVisible = false
                onPresetClick()
            },
            onVesselSaved = {
                vesselAddVisible = false
            }
        )
    }

    TaskTopScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPresetClick = onPresetClick,
        onAlarmClick = onAlarmClick,
        onVesselClick = onVesselClick,
        onAddVesselClick = { vesselAddVisible = true },
        onDataManageClick = onDataManageClick,
        onGuideClick = onGuideClick,
        onAlarmToggle = { checked ->
            viewModel.onAlarmToggle(checked)
            if (checked) {
                TaskAlarmScheduler.syncAll(context)
            } else {
                TaskAlarmScheduler.cancelAll(context)
            }
        },
        onVesselToggle = viewModel::onVesselToggle,
        onHomeClick = onHomeClick,
        onKorClick = onKorClick,
        onEngClick = onEngClick,
        onContactClick = onContactClick,
        onExitClick = onExitClick
    )
}
