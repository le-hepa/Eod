package com.gomgom.eod.feature.task.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.task.screens.TaskTopScreen
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel

@Composable
fun TaskToolWrapper(
    onBackClick: () -> Unit,
    onPresetClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onVesselClick: (Long) -> Unit,
    onAddVesselClick: () -> Unit,
    onDataManageClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val viewModel: TaskTopViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()

    TaskTopScreen(
        uiState = uiState.copy(presetGroups = presetGroups),
        onBackClick = onBackClick,
        onPresetClick = onPresetClick,
        onAlarmClick = onAlarmClick,
        onVesselClick = onVesselClick,
        onAddVesselClick = onAddVesselClick,
        onDataManageClick = onDataManageClick,
        onAlarmToggle = viewModel::onAlarmToggle,
        onVesselToggle = viewModel::onVesselToggle,
        onHomeClick = onHomeClick,
        onKorClick = onKorClick,
        onEngClick = onEngClick,
        onContactClick = onContactClick,
        onExitClick = onExitClick
    )
}