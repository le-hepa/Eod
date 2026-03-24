package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkViewModel

private val WorkAddBackground = Color(0xFFF5F8FC)
private val WorkAddCardColor = Color.White
private val WorkAddPrimaryText = Color(0xFF123A73)
private val WorkAddSecondaryText = Color(0xFF6E85A3)
private val WorkAddDivider = Color(0xFFDCE5F0)

@Composable
fun TaskPresetWorkAddScreen(
    presetId: Long,
    onBackClick: () -> Unit,
    onWorkSaved: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val viewModel: TaskPresetWorkViewModel = viewModel()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()
    val presetName = presetGroups.firstOrNull { it.id == presetId }?.name ?: ""

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var saveDialogVisible by remember { mutableStateOf(false) }
    var duplicateDialogVisible by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var cycleNumber by remember { mutableStateOf("") }
    var cycleUnit by remember { mutableStateOf(CycleUnit.DAY) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    if (saveDialogVisible) {
        AlertDialog(
            onDismissRequest = { saveDialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveDialogVisible = false
                        val saved = viewModel.addWork(
                            presetId = presetId,
                            name = name,
                            reference = reference,
                            cycleNumber = cycleNumber.toIntOrNull() ?: 0,
                            cycleUnit = cycleUnit
                        )
                        if (saved != null) {
                            onWorkSaved()
                        }
                    }
                ) {
                    Text(stringResource(R.string.task_preset_work_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { saveDialogVisible = false }) {
                    Text(stringResource(R.string.task_preset_work_confirm_no))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.task_preset_work_save_confirm_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.task_preset_work_save_confirm_body),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            containerColor = Color.White
        )
    }

    if (duplicateDialogVisible) {
        AlertDialog(
            onDismissRequest = { duplicateDialogVisible = false },
            confirmButton = {
                TextButton(onClick = { duplicateDialogVisible = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            title = { Text(stringResource(R.string.task_preset_work_duplicate_title)) },
            text = { Text(stringResource(R.string.task_preset_work_duplicate_body_add)) },
            containerColor = Color.White
        )
    }

    if (appInfoVisible) {
        WorkAddPopupFrame(
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

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = WorkAddBackground,
        topBar = {
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
                            tint = WorkAddPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = presetName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkAddPrimaryText
                    )
                }

                TaskHamburgerMenuButton(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    iconTint = WorkAddPrimaryText,
                    menuBackgroundColor = WorkAddCardColor,
                    dividerColor = WorkAddDivider,
                    textColor = WorkAddPrimaryText,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WorkAddBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(top = 18.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WorkAddCardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.task_preset_work_detail_title_add),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkAddPrimaryText,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = WorkAddDivider)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = taskOutlinedTextFieldColors(WorkAddPrimaryText, WorkAddSecondaryText),
                        singleLine = false,
                        minLines = 1,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.task_preset_work_name_hint),
                                color = WorkAddSecondaryText
                            )
                        }
                    )

                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = taskOutlinedTextFieldColors(WorkAddPrimaryText, WorkAddSecondaryText),
                        singleLine = false,
                        minLines = 1,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.task_preset_work_reference_hint),
                                color = WorkAddSecondaryText
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cycleNumber,
                            onValueChange = { cycleNumber = it.filter(Char::isDigit) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = taskOutlinedTextFieldColors(WorkAddPrimaryText, WorkAddSecondaryText),
                            textStyle = TextStyle(textAlign = TextAlign.Center),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.task_preset_work_cycle_number_hint),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = WorkAddSecondaryText
                                )
                            }
                        )

                        Box {
                            TextButton(
                                onClick = { unitMenuExpanded = true },
                                modifier = Modifier
                                    .background(Color(0xFFEAF2FF), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = toCycleLabel(cycleUnit),
                                    color = WorkAddPrimaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false }
                            ) {
                                CycleUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(toCycleLabel(unit)) },
                                        onClick = {
                                            cycleUnit = unit
                                            unitMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (viewModel.isDuplicateNameInPreset(presetId = presetId, name = name)) {
                                    duplicateDialogVisible = true
                                } else if (name.trim().isNotBlank() && (cycleNumber.toIntOrNull() ?: 0) > 0) {
                                    saveDialogVisible = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = stringResource(R.string.task_preset_work_save),
                                tint = WorkAddPrimaryText,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun toCycleLabel(unit: CycleUnit): String {
    return when (unit) {
        CycleUnit.DAY -> stringResource(R.string.cycle_unit_day)
        CycleUnit.WEEK -> stringResource(R.string.cycle_unit_week)
        CycleUnit.MONTH -> stringResource(R.string.cycle_unit_month)
        CycleUnit.YEAR -> stringResource(R.string.cycle_unit_year)
    }
}

@Composable
private fun WorkAddPopupFrame(
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
            colors = CardDefaults.cardColors(containerColor = WorkAddCardColor),
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
                    color = WorkAddPrimaryText
                )

                HorizontalDivider(color = WorkAddDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = WorkAddDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = WorkAddPrimaryText,
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
            color = WorkAddSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = WorkAddPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}


