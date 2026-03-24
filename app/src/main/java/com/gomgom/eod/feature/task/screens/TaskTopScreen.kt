package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.R
import com.gomgom.eod.core.common.AutoResizeText
import com.gomgom.eod.feature.task.viewmodel.TaskTopUiState
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskTopVesselItem

private val TaskBackground = Color(0xFFF5F8FC)
private val TaskCardColor = Color.White
private val TaskAccentSurface = Color(0xFFEAF2FF)
private val TaskPrimaryText = Color(0xFF123A73)
private val TaskSecondaryText = Color(0xFF6E85A3)
private val TaskDivider = Color(0xFFDCE5F0)
private val TaskSwitchCheckedTrack = Color(0xFF2E6CEB)
private val TaskSwitchUncheckedTrack = Color(0xFFD6E0EC)

private val TaskTopBarSideSize = 42.dp
private val TaskCardRadius = 26.dp
private val TaskCardSpacing = 12.dp
private val TaskCardHorizontalPadding = 18.dp
private val TaskCardVerticalPadding = 16.dp
private val TaskCardTextSize = 20.sp
private val TaskCardLineHeight = 24.sp

@Composable
fun TaskTopScreen(
    uiState: TaskTopUiState,
    onBackClick: () -> Unit,
    onPresetClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onVesselClick: (Long) -> Unit,
    onAddVesselClick: () -> Unit,
    onDataManageClick: () -> Unit,
    onGuideClick: () -> Unit,
    onAlarmToggle: (Boolean) -> Unit,
    onVesselToggle: (Long, Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val taskTopViewModel: TaskTopViewModel = viewModel()
    val activePresetName = uiState.presetGroups.firstOrNull { it.enabled }?.name
    val presetTitle = if (activePresetName.isNullOrBlank()) {
        stringResource(R.string.task_top_preset)
    } else {
        stringResource(R.string.task_top_preset_with_name, activePresetName)
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var manageTarget by remember { mutableStateOf<TaskTopVesselItem?>(null) }
    var renameTarget by remember { mutableStateOf<TaskTopVesselItem?>(null) }
    var deleteTarget by remember { mutableStateOf<TaskTopVesselItem?>(null) }
    var renameValue by remember { mutableStateOf("") }

    if (appInfoVisible) {
        TaskPopupFrame(
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

    manageTarget?.let { vessel ->
        TaskPopupFrame(
            title = vessel.name,
            confirmText = stringResource(R.string.common_close),
            onDismiss = { manageTarget = null }
        ) {
            TextButton(
                onClick = {
                    renameTarget = vessel
                    renameValue = vessel.name
                    manageTarget = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.common_edit),
                    color = TaskPrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = {
                    deleteTarget = vessel
                    manageTarget = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.common_delete),
                    color = TaskPrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    renameTarget?.let { vessel ->
        TaskVesselRenameDialog(
            vesselName = renameValue,
            onValueChange = { renameValue = it },
            onDismiss = {
                renameTarget = null
                renameValue = ""
            },
            onConfirm = {
                taskTopViewModel.updateVesselName(vessel.id, renameValue)
                renameTarget = null
                renameValue = ""
            }
        )
    }

    deleteTarget?.let { vessel ->
        TaskVesselDeleteDialog(
            vesselName = vessel.name,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                taskTopViewModel.deleteVessel(vessel.id)
                deleteTarget = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = TaskBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(TaskTopBarSideSize),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(TaskTopBarSideSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_close),
                            tint = TaskPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.task_top_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskPrimaryText,
                        textAlign = TextAlign.Center
                    )
                }

                TaskHamburgerMenuButton(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    iconTint = TaskPrimaryText,
                    menuBackgroundColor = TaskCardColor,
                    dividerColor = TaskDivider,
                    textColor = TaskPrimaryText,
                    onHomeClick = onHomeClick,
                    onKorClick = onKorClick,
                    onEngClick = onEngClick,
                    onAppInfoClick = { appInfoVisible = true },
                    onGuideClick = onGuideClick,
                    onContactClick = onContactClick,
                    onExitClick = onExitClick
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TaskBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(TaskCardSpacing)
        ) {
            item {
                CenterTextCard(
                    text = presetTitle,
                    onClick = onPresetClick
                )
            }

            item {
                CenterTextCard(
                    text = stringResource(R.string.task_top_alarm),
                    onClick = onAlarmClick,
                    trailing = {
                        TaskSwitch(
                            checked = uiState.alarmEnabled,
                            onCheckedChange = onAlarmToggle
                        )
                    }
                )
            }

            if (uiState.vesselItems.isNotEmpty()) {
                items(uiState.vesselItems) { vessel ->
                    VesselCard(
                        vesselItem = vessel,
                        onClick = { onVesselClick(vessel.id) },
                        onLongClick = { manageTarget = vessel },
                        onToggle = { checked -> onVesselToggle(vessel.id, checked) }
                    )
                }
            }

            item {
                AddVesselCard(
                    onClick = onAddVesselClick
                )
            }

            item {
                DataManageRow(
                    onClick = onDataManageClick
                )
            }
        }
    }
}

@Composable
private fun CenterTextCard(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(TaskCardRadius),
        colors = CardDefaults.cardColors(containerColor = TaskCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TaskCardHorizontalPadding,
                    vertical = TaskCardVerticalPadding
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeText(
                    text = text,
                    modifier = if (trailing != null) Modifier.padding(horizontal = 44.dp) else Modifier,
                    style = TextStyle(
                        fontSize = TaskCardTextSize,
                        lineHeight = TaskCardLineHeight,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) TaskPrimaryText else TaskSecondaryText,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2,
                    minFontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (trailing != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailing()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VesselCard(
    vesselItem: TaskTopVesselItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(TaskCardRadius),
        colors = CardDefaults.cardColors(containerColor = TaskCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TaskCardHorizontalPadding,
                    vertical = TaskCardVerticalPadding
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeText(
                    text = vesselItem.name,
                    modifier = Modifier.padding(horizontal = 44.dp),
                    style = TextStyle(
                        fontSize = TaskCardTextSize,
                        lineHeight = TaskCardLineHeight,
                        fontWeight = FontWeight.SemiBold,
                        color = TaskPrimaryText,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2,
                    minFontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                TaskSwitch(
                    checked = vesselItem.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun AddVesselCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(TaskCardRadius),
        colors = CardDefaults.cardColors(containerColor = TaskAccentSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TaskCardHorizontalPadding,
                    vertical = TaskCardVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(TaskPrimaryText, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.task_top_add_vessel),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = stringResource(R.string.task_top_add_vessel),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TaskPrimaryText,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun DataManageRow(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 6.dp, start = 4.dp, end = 4.dp, bottom = 14.dp)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = TaskDivider
        )

        Text(
            text = stringResource(R.string.task_top_data_manage),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TaskSecondaryText,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun TaskSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.scale(0.6f),
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = TaskSwitchCheckedTrack,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = TaskSwitchUncheckedTrack
        )
    )
}

@Composable
private fun TaskPopupFrame(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TaskCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskPrimaryText,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = TaskDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = TaskDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = TaskPrimaryText,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TaskSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = TaskPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PopupGuideLine(
    text: String
) {
    Text(
        text = "• $text",
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TaskPrimaryText
    )
}

@Composable
private fun TaskVesselRenameDialog(
    vesselName: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TaskCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_edit),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskPrimaryText,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = vesselName,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = taskOutlinedTextFieldColors(TaskPrimaryText, TaskSecondaryText),
                    label = {
                        Text(
                            text = stringResource(R.string.task_top_vessel_name_hint),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            color = TaskSecondaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(
                        onClick = onConfirm,
                        enabled = vesselName.trim().isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.common_save),
                            color = TaskPrimaryText,
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
private fun TaskVesselDeleteDialog(
    vesselName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TaskCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = vesselName,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskPrimaryText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.common_delete),
                    fontSize = 15.sp,
                    color = TaskSecondaryText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            color = TaskSecondaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = stringResource(R.string.common_delete),
                            color = TaskPrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
