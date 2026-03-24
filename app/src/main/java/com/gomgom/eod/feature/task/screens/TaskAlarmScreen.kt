package com.gomgom.eod.feature.task.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.R
import com.gomgom.eod.feature.task.alarm.TaskAlarmScheduler
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplayFilter
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplayItem
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplaySort
import com.gomgom.eod.feature.task.repository.TaskAlarmDisplayType
import com.gomgom.eod.feature.task.viewmodel.TaskAlarmSettingsViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskWorkRecordViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel
import java.time.LocalTime

private val AlarmBackground = Color(0xFFF5F8FC)
private val AlarmCardColor = Color.White
private val AlarmRegularColor = Color(0xFFEAF2FF)
private val AlarmIrregularColor = Color(0xFFFFF1E6)
private val AlarmAccentSelection = Color(0xFFDCE8FF)
private val AlarmPrimaryText = Color(0xFF123A73)
private val AlarmSecondaryText = Color(0xFF6E85A3)
private val AlarmSwitchCheckedTrack = Color(0xFF2E6CEB)
private val AlarmSwitchUncheckedTrack = Color(0xFFD6E0EC)

private enum class AlarmSortOption {
    BASIC,
    A_TO_Z,
    Z_TO_A,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

private enum class AlarmFilterOption {
    ALL,
    REGULAR,
    IRREGULAR
}
private enum class AlarmWorkType {
    REGULAR,
    IRREGULAR
}
private data class AlarmWorkRowItem(
    val id: Long,
    val name: String,
    val cycleNumber: Int?,
    val cycleUnit: CycleUnit?,
    val alarmEnabled: Boolean,
    val type: AlarmWorkType
)

@Composable
fun TaskAlarmScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TaskTopViewModel = viewModel()
    val workViewModel: TaskPresetWorkViewModel = viewModel()
    val workRecordViewModel: TaskWorkRecordViewModel = viewModel()
    val alarmSettingsViewModel: TaskAlarmSettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val alarmSettings by alarmSettingsViewModel.settings.collectAsState()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(AlarmSortOption.BASIC) }
    var filterOption by remember { mutableStateOf(AlarmFilterOption.ALL) }
    var detailTarget by remember { mutableStateOf<TaskPresetWorkItem?>(null) }
    var deleteConfirmVisible by remember { mutableStateOf(false) }
    var timePickerVisible by remember { mutableStateOf(false) }
    var permissionGuideVisible by remember { mutableStateOf(false) }
    var pendingEnableAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingEnableAction?.invoke()
        }
        pendingEnableAction = null
        permissionGuideVisible = false
    }

    val activePreset = presetGroups.firstOrNull { it.enabled }
    val activePresetId = activePreset?.id ?: 0L
    val activeVessel = uiState.vesselItems.firstOrNull { it.enabled }
    val activeVesselId = activeVessel?.id
    val presetWorks by workViewModel.worksForPreset(activePresetId).collectAsState(initial = emptyList())
    val irregularCandidateRecords by workRecordViewModel
        .irregularAlarmCandidatesForVesselFlow(activeVesselId ?: 0L)
        .collectAsState(initial = emptyList())
    val currentAlarmTime = remember(alarmSettings.alarmTime) {
        alarmSettings.alarmTime.split(":").let { parts ->
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
        }
    }

    val requestNotificationPermissionIfNeeded: ((() -> Unit) -> Unit) = { onGranted ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onGranted()
        } else if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            pendingEnableAction = onGranted
            permissionGuideVisible = true
        }
    }

    if (permissionGuideVisible) {
        AlertDialog(
            onDismissRequest = {
                permissionGuideVisible = false
                pendingEnableAction = null
            },
            confirmButton = {
                TextButton(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        permissionGuideVisible = false
                        pendingEnableAction = null
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            title = { Text(stringResource(R.string.task_alarm_permission_title)) },
            text = {
                Text(
                    text = stringResource(R.string.task_alarm_permission_body),
                    textAlign = TextAlign.Center
                )
            },
            containerColor = Color.White
        )
    }

    if (appInfoVisible) {
        AlarmPopupFrame(
            title = stringResource(R.string.home_app_info_title),
            confirmText = stringResource(R.string.home_app_info_confirm),
            onDismiss = { appInfoVisible = false }
        ) {
            PopupInfoRow(
                label = stringResource(R.string.home_app_info_name_label),
                value = stringResource(R.string.home_app_info_name_value)
            )
            PopupInfoRow(
                label = stringResource(R.string.home_app_info_version_label),
                value = stringResource(R.string.home_app_info_version_value)
            )
        }
    }

    if (timePickerVisible) {
        AlarmTimePickerDialog(
            initialHour = currentAlarmTime.first,
            initialMinute = currentAlarmTime.second,
            initialTimeText = alarmSettings.alarmTime,
            onDismiss = { timePickerVisible = false },
            onConfirm = { _, _, normalized ->
                alarmSettingsViewModel.setAlarmTime(normalized)
                TaskAlarmScheduler.syncAll(context)
                timePickerVisible = false
            }
        )
    }

    val regularAlarmItems by remember(activePreset, presetWorks, alarmSettings) {
        derivedStateOf {
            if (activePreset == null) {
                emptyList()
            } else {
                presetWorks.map { work ->
                    AlarmWorkRowItem(
                        id = work.id,
                        name = work.name,
                        cycleNumber = work.cycleNumber.takeIf { it > 0 },
                        cycleUnit = work.cycleUnit.takeIf { work.cycleNumber > 0 },
                        alarmEnabled = alarmSettingsViewModel.isRegularWorkAlarmEnabled(
                            work.id,
                            work.alarmEnabled
                        ),
                        type = AlarmWorkType.REGULAR
                    )
                }
            }
        }
    }
    val irregularAlarmItems by remember(activeVesselId, irregularCandidateRecords, alarmSettings) {
        derivedStateOf {
            if (activeVesselId == null) {
                emptyList()
            } else {
                irregularCandidateRecords
                    .mapNotNull { latestRecord ->
                        val trimmedName = latestRecord.workName.trim()
                        if (trimmedName.isBlank()) return@mapNotNull null
                        AlarmWorkRowItem(
                            id = -latestRecord.id,
                            name = trimmedName,
                            cycleNumber = latestRecord.cycleNumberText.trim().toIntOrNull(),
                            cycleUnit = shortCycleUnitToEnum(latestRecord.cycleUnitText),
                            alarmEnabled = alarmSettingsViewModel.isIrregularWorkAlarmEnabled(
                                activeVesselId,
                                trimmedName,
                                true
                            ),
                            type = AlarmWorkType.IRREGULAR
                        )
                    }
            }
        }
    }
    val connectedAlarmItems by remember(regularAlarmItems, irregularAlarmItems) {
        derivedStateOf { regularAlarmItems + irregularAlarmItems }
    }
    val visibleItems by remember(connectedAlarmItems, filterOption, sortOption) {
        derivedStateOf {
            workRecordViewModel.buildAlarmVisibleItems(
                items = connectedAlarmItems.map { it.toDisplayItem() },
                filter = filterOption.toDisplayFilter(),
                sort = sortOption.toDisplaySort()
            ).map { it.toRowItem() }
        }
    }

    if (deleteConfirmVisible && detailTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val deleted = workViewModel.deleteWork(detailTarget!!.id)
                        deleteConfirmVisible = false
                        if (deleted) {
                            detailTarget = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.task_preset_work_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmVisible = false }) {
                    Text(stringResource(R.string.task_preset_work_confirm_no))
                }
            },
            title = { Text(stringResource(R.string.task_preset_work_delete_confirm_title)) },
            text = { Text(stringResource(R.string.task_preset_work_delete_confirm_body)) },
            containerColor = Color.White
        )
    }

    detailTarget?.let { work ->
        AlarmWorkDetailDialog(
            work = work,
            onDismiss = { detailTarget = null },
            onDelete = { deleteConfirmVisible = true }
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = AlarmBackground,
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_close),
                                tint = AlarmPrimaryText,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.task_top_alarm),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlarmPrimaryText,
                            textAlign = TextAlign.Center
                        )
                    }

                    TaskHamburgerMenuButton(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it },
                        iconTint = AlarmPrimaryText,
                        menuBackgroundColor = AlarmCardColor,
                        dividerColor = AlarmSwitchUncheckedTrack,
                        textColor = AlarmPrimaryText,
                        onHomeClick = onHomeClick,
                        onKorClick = onKorClick,
                        onEngClick = onEngClick,
                        onAppInfoClick = { appInfoVisible = true },
                        onGuideClick = onGuideClick,
                        onContactClick = onContactClick,
                        onExitClick = onExitClick
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Switch(
                            checked = uiState.alarmEnabled,
                            onCheckedChange = { checked ->
                                val applyChange = {
                                    viewModel.onAlarmToggle(checked)
                                    if (checked) {
                                        TaskAlarmScheduler.syncAll(context)
                                    } else {
                                        TaskAlarmScheduler.cancelAll(context)
                                    }
                                }
                                if (checked) requestNotificationPermissionIfNeeded(applyChange) else applyChange()
                            },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AlarmSwitchCheckedTrack,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = AlarmSwitchUncheckedTrack
                            )
                        )
                    }

                    Box {
                        TextButton(
                            onClick = { sortExpanded = true },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sort,
                                contentDescription = stringResource(R.string.task_alarm_sort_content_description),
                                tint = AlarmPrimaryText,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false },
                            modifier = Modifier.background(AlarmCardColor)
                        ) {
                            AlarmSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(alarmSortOptionLabel(option), color = AlarmPrimaryText) },
                                    onClick = {
                                        sortOption = option
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        TextButton(
                            onClick = { filterExpanded = true },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = stringResource(R.string.task_alarm_filter_content_description),
                                tint = AlarmPrimaryText,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.background(AlarmCardColor)
                        ) {
                            AlarmFilterOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(alarmFilterOptionLabel(option), color = AlarmPrimaryText) },
                                    onClick = {
                                        filterOption = option
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AlarmBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                AlarmTimeSettingCard(
                    timeText = alarmSettings.alarmTime,
                    onClick = { timePickerVisible = true }
                )
            }
            item {
                Text(
                    text = stringResource(R.string.alert_list_sort_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = AlarmSecondaryText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End
                )
            }
            item {
                AlarmHeaderRow()
            }

            if (connectedAlarmItems.isEmpty()) {
                item {
                    EmptyAlarmCard(message = stringResource(R.string.alert_list_empty))
                }
            } else {
                if (visibleItems.isEmpty()) {
                    item {
                        EmptyAlarmCard(message = stringResource(R.string.alert_list_filtered_empty))
                    }
                } else items(visibleItems, key = { it.id }) { item ->
                    AlarmWorkCard(
                        item = item,
                        onClick = {
                            if (item.type == AlarmWorkType.REGULAR) {
                                detailTarget = presetWorks.firstOrNull { work -> work.id == item.id }
                            }
                        },
                        onAlarmToggle = { checked ->
                            if (item.type == AlarmWorkType.REGULAR) {
                                val applyChange = {
                                    workViewModel.updateAlarmEnabled(item.id, checked)
                                    alarmSettingsViewModel.setRegularWorkAlarmEnabled(item.id, checked)
                                }
                                if (checked) requestNotificationPermissionIfNeeded(applyChange) else applyChange()
                            } else {
                                val applyChange = {
                                    activeVesselId?.let { vesselId ->
                                        alarmSettingsViewModel.setIrregularWorkAlarmEnabled(
                                            vesselId = vesselId,
                                            workName = item.name.trim(),
                                            enabled = checked
                                        )
                                    }
                                    Unit
                                }
                                if (checked) requestNotificationPermissionIfNeeded(applyChange) else applyChange()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmHeaderRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.alert_list_column_work),
                modifier = Modifier.weight(4.8f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AlarmSecondaryText
            )
            Text(
                text = stringResource(R.string.alert_list_column_cycle),
                modifier = Modifier.weight(1.8f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AlarmSecondaryText
            )
            Text(
                text = stringResource(R.string.alert_list_column_alarm),
                modifier = Modifier.weight(1.8f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AlarmSecondaryText
            )
        }
    }
}

@Composable
private fun AlarmTimeSettingCard(
    timeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = stringResource(R.string.alert_list_time_setting, timeText),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
            color = AlarmPrimaryText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AlarmWorkCard(
    item: AlarmWorkRowItem,
    onClick: () -> Unit,
    onAlarmToggle: (Boolean) -> Unit
) {
    val backgroundColor = when (item.type) {
        AlarmWorkType.REGULAR -> AlarmRegularColor
        AlarmWorkType.IRREGULAR -> AlarmIrregularColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(4.8f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlarmPrimaryText,
                    maxLines = 2,
                    textAlign = TextAlign.Start
                )
            }

            Text(
                text = formatCycle(item.cycleNumber, item.cycleUnit),
                modifier = Modifier.weight(1.8f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AlarmPrimaryText
            )

            Box(
                modifier = Modifier.weight(1.8f),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    checked = item.alarmEnabled,
                    onCheckedChange = onAlarmToggle,
                    modifier = Modifier.scale(0.74f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AlarmSwitchCheckedTrack,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = AlarmSwitchUncheckedTrack,
                    )
                )
            }
        }
    }
}

@Composable
private fun AlarmWorkDetailDialog(
    work: TaskPresetWorkItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.task_preset_work_detail_title_edit),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlarmPrimaryText,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = AlarmSwitchUncheckedTrack)

                OutlinedTextField(
                    value = work.name,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    readOnly = true,
                    singleLine = false,
                    minLines = 1,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.task_preset_work_name_hint),
                            color = AlarmSecondaryText
                        )
                    }
                )

                OutlinedTextField(
                    value = work.reference,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    readOnly = true,
                    singleLine = false,
                    minLines = 1,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.task_preset_work_reference_hint),
                            color = AlarmSecondaryText
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = work.cycleNumber.toString(),
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.task_preset_work_cycle_number_hint),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = AlarmSecondaryText
                            )
                        }
                    )

                    OutlinedTextField(
                        value = toCycleLabel(work.cycleUnit),
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.common_close),
                            color = AlarmSecondaryText
                        )
                    }
                    TextButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.task_preset_work_delete),
                            tint = AlarmPrimaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmPopupFrame(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlarmPrimaryText
                )
                HorizontalDivider(color = AlarmSwitchUncheckedTrack)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    content()
                }
                HorizontalDivider(color = AlarmSwitchUncheckedTrack)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = AlarmPrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AlarmSecondaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = AlarmPrimaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PopupGuideLine(
    text: String
) {
    Text(
        text = text,
        color = AlarmPrimaryText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun AlarmTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    initialTimeText: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    var selectedHour by remember(initialHour) { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember(initialMinute) { mutableStateOf(initialMinute.coerceIn(0, 59)) }
    var directInputEnabled by remember(initialTimeText) { mutableStateOf(false) }
    var directTimeText by remember(initialTimeText) { mutableStateOf(initialTimeText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.width(320.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.alert_list_time_picker_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { directInputEnabled = !directInputEnabled },
                    color = AlarmPrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (directInputEnabled) {
                    OutlinedTextField(
                        value = directTimeText,
                        onValueChange = { changed ->
                            val filtered = changed.filterIndexed { index, char ->
                                char.isDigit() || (char == ':' && index == 2)
                            }.take(5)
                            directTimeText = filtered
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = taskOutlinedTextFieldColors(AlarmPrimaryText, AlarmSecondaryText),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                        placeholder = {
                            Text(
                                text = "HH:MM",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = AlarmSecondaryText
                            )
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlarmWheelColumn(
                        modifier = Modifier.weight(1f),
                        values = (0..23).toList(),
                        selectedValue = selectedHour,
                        formatter = { "%02d".format(it) },
                        onSelect = { selectedHour = it }
                    )
                    AlarmWheelColumn(
                        modifier = Modifier.weight(1f),
                        values = (0..59).toList(),
                        selectedValue = selectedMinute,
                        formatter = { "%02d".format(it) },
                        onSelect = { selectedMinute = it }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.common_cancel), color = AlarmPrimaryText)
                    }
                    TextButton(
                        onClick = {
                            val normalized = if (directInputEnabled) {
                                normalizeAlarmTimeInput(directTimeText)
                            } else {
                                "%02d:%02d".format(selectedHour, selectedMinute)
                            } ?: return@TextButton
                            val normalizedParts = normalized.split(":")
                            onConfirm(
                                normalizedParts[0].toInt(),
                                normalizedParts[1].toInt(),
                                normalized
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.common_ok), color = AlarmPrimaryText)
                    }
                }
            }
        }
    }
}

private fun normalizeAlarmTimeInput(raw: String): String? {
    val trimmed = raw.trim()
    val match = Regex("^(\\d{2}):(\\d{2})$").matchEntire(trimmed) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return "%02d:%02d".format(hour, minute)
}

@Composable
private fun AlarmWheelColumn(
    modifier: Modifier = Modifier,
    values: List<Int>,
    selectedValue: Int,
    formatter: (Int) -> String,
    onSelect: (Int) -> Unit
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AlarmRegularColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = values,
                key = { it }
            ) { value ->
                val selected = value == selectedValue
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { onSelect(value) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) AlarmAccentSelection else AlarmCardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = formatter(value),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = AlarmPrimaryText,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun alarmSortOptionLabel(option: AlarmSortOption): String {
    return when (option) {
        AlarmSortOption.BASIC -> stringResource(R.string.task_alarm_sort_basic)
        AlarmSortOption.A_TO_Z -> stringResource(R.string.task_alarm_sort_a_to_z)
        AlarmSortOption.Z_TO_A -> stringResource(R.string.task_alarm_sort_z_to_a)
        AlarmSortOption.DAY -> stringResource(R.string.task_alarm_sort_day)
        AlarmSortOption.WEEK -> stringResource(R.string.task_alarm_sort_week)
        AlarmSortOption.MONTH -> stringResource(R.string.task_alarm_sort_month)
        AlarmSortOption.YEAR -> stringResource(R.string.task_alarm_sort_year)
    }
}

@Composable
private fun alarmFilterOptionLabel(option: AlarmFilterOption): String {
    return when (option) {
        AlarmFilterOption.ALL -> stringResource(R.string.task_alarm_filter_all)
        AlarmFilterOption.REGULAR -> stringResource(R.string.task_alarm_filter_regular)
        AlarmFilterOption.IRREGULAR -> stringResource(R.string.task_alarm_filter_irregular)
    }
}

@Composable
private fun toCycleLabel(unit: CycleUnit): String {
    return when (unit) {
        CycleUnit.DAY -> stringResource(R.string.task_alarm_cycle_day)
        CycleUnit.WEEK -> stringResource(R.string.task_alarm_cycle_week)
        CycleUnit.MONTH -> stringResource(R.string.task_alarm_cycle_month)
        CycleUnit.YEAR -> stringResource(R.string.task_alarm_cycle_year)
    }
}

private fun shortCycleUnitToEnum(shortLabel: String): CycleUnit? {
    return when (shortLabel.trim().uppercase()) {
        "D" -> CycleUnit.DAY
        "W" -> CycleUnit.WEEK
        "M" -> CycleUnit.MONTH
        "Y" -> CycleUnit.YEAR
        else -> null
    }
}

@Composable
private fun EmptyAlarmCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlarmCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = AlarmSecondaryText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

private fun AlarmWorkRowItem.toDisplayItem(): TaskAlarmDisplayItem {
    return TaskAlarmDisplayItem(
        id = id,
        name = name,
        cycleNumber = cycleNumber,
        cycleUnit = cycleUnit,
        alarmEnabled = alarmEnabled,
        type = when (type) {
            AlarmWorkType.REGULAR -> TaskAlarmDisplayType.REGULAR
            AlarmWorkType.IRREGULAR -> TaskAlarmDisplayType.IRREGULAR
        }
    )
}

private fun TaskAlarmDisplayItem.toRowItem(): AlarmWorkRowItem {
    return AlarmWorkRowItem(
        id = id,
        name = name,
        cycleNumber = cycleNumber,
        cycleUnit = cycleUnit,
        alarmEnabled = alarmEnabled,
        type = when (type) {
            TaskAlarmDisplayType.REGULAR -> AlarmWorkType.REGULAR
            TaskAlarmDisplayType.IRREGULAR -> AlarmWorkType.IRREGULAR
        }
    )
}

private fun AlarmFilterOption.toDisplayFilter(): TaskAlarmDisplayFilter {
    return when (this) {
        AlarmFilterOption.ALL -> TaskAlarmDisplayFilter.ALL
        AlarmFilterOption.REGULAR -> TaskAlarmDisplayFilter.REGULAR
        AlarmFilterOption.IRREGULAR -> TaskAlarmDisplayFilter.IRREGULAR
    }
}

private fun AlarmSortOption.toDisplaySort(): TaskAlarmDisplaySort {
    return when (this) {
        AlarmSortOption.BASIC -> TaskAlarmDisplaySort.BASIC
        AlarmSortOption.A_TO_Z -> TaskAlarmDisplaySort.A_TO_Z
        AlarmSortOption.Z_TO_A -> TaskAlarmDisplaySort.Z_TO_A
        AlarmSortOption.DAY -> TaskAlarmDisplaySort.DAY
        AlarmSortOption.WEEK -> TaskAlarmDisplaySort.WEEK
        AlarmSortOption.MONTH -> TaskAlarmDisplaySort.MONTH
        AlarmSortOption.YEAR -> TaskAlarmDisplaySort.YEAR
    }
}

@Composable
private fun formatCycle(
    cycleNumber: Int?,
    cycleUnit: CycleUnit?
): String {
    if (cycleNumber == null || cycleUnit == null || cycleNumber <= 0) {
        return "-"
    }

    val unitLabel = when (cycleUnit) {
        CycleUnit.DAY -> stringResource(R.string.task_alarm_cycle_short_day)
        CycleUnit.WEEK -> stringResource(R.string.task_alarm_cycle_short_week)
        CycleUnit.MONTH -> stringResource(R.string.task_alarm_cycle_short_month)
        CycleUnit.YEAR -> stringResource(R.string.task_alarm_cycle_short_year)
    }

    return "$cycleNumber $unitLabel"
}
