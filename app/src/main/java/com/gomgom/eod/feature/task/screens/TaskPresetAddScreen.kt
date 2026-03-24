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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.gomgom.eod.feature.task.viewmodel.TaskPresetViewModel

private val PresetAddBackground = Color(0xFFF5F8FC)
private val PresetAddCardColor = Color.White
private val PresetAddPrimaryText = Color(0xFF123A73)
private val PresetAddSecondaryText = Color(0xFF6E85A3)
private val PresetAddDivider = Color(0xFFDCE5F0)
private val PresetAddAccentSurface = Color(0xFFEAF2FF)

@Composable
fun TaskPresetAddScreen(
    onBackClick: () -> Unit,
    onPresetSaved: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val viewModel: TaskPresetViewModel = viewModel()

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

    if (appInfoVisible) {
        PresetAddPopupFrame(
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
        containerColor = PresetAddBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(42.dp)
                ) {
                        Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_close),
                            tint = PresetAddPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                }

                Text(
                    text = stringResource(R.string.task_preset_top_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PresetAddPrimaryText
                )

                TaskHamburgerMenuButton(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    iconTint = PresetAddPrimaryText,
                    menuBackgroundColor = PresetAddCardColor,
                    dividerColor = PresetAddDivider,
                    textColor = PresetAddPrimaryText,
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
                .background(PresetAddBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(top = 18.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = PresetAddCardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                TaskPresetAddForm(
                    presetName = presetName,
                    onPresetNameChange = { presetName = it },
                    onDismiss = onBackClick,
                    onSave = {
                        val createdId = viewModel.addPreset(presetName)
                        if (createdId != null) {
                            onPresetSaved()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskPresetAddDialog(
    onDismiss: () -> Unit,
    onPresetSaved: () -> Unit
) {
    val viewModel: TaskPresetViewModel = viewModel()
    var presetName by remember { mutableStateOf("") }

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
            colors = CardDefaults.cardColors(containerColor = PresetAddCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            TaskPresetAddForm(
                presetName = presetName,
                onPresetNameChange = { presetName = it },
                onDismiss = onDismiss,
                onSave = {
                    val createdId = viewModel.addPreset(presetName)
                    if (createdId != null) {
                        onPresetSaved()
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskPresetAddForm(
    presetName: String,
    onPresetNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.task_preset_top_add),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PresetAddPrimaryText,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(color = PresetAddDivider)

        OutlinedTextField(
            value = presetName,
            onValueChange = onPresetNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = taskOutlinedTextFieldColors(PresetAddPrimaryText, PresetAddSecondaryText),
            textStyle = TextStyle(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = stringResource(R.string.task_preset_top_name_hint),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = PresetAddSecondaryText
                )
            }
        )

        Text(
            text = stringResource(R.string.task_preset_top_name_example),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            color = PresetAddSecondaryText,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onSave,
                enabled = presetName.trim().isNotEmpty(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PresetAddPrimaryText,
                    contentColor = Color.White,
                    disabledContainerColor = PresetAddAccentSurface,
                    disabledContentColor = PresetAddSecondaryText
                )
            ) {
                Text(
                    text = stringResource(R.string.task_preset_top_add_confirm),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PresetAddPopupFrame(
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
            colors = CardDefaults.cardColors(containerColor = PresetAddCardColor),
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
                    color = PresetAddPrimaryText
                )

                HorizontalDivider(color = PresetAddDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = PresetAddDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = PresetAddPrimaryText,
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
            color = PresetAddSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = PresetAddPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}
