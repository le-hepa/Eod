package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.gomgom.eod.core.common.AutoResizeText
import com.gomgom.eod.feature.task.viewmodel.TaskPresetGroupItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetViewModel

private val PresetBackground = Color(0xFFF5F8FC)
private val PresetCardColor = Color.White
private val PresetAccentSurface = Color(0xFFEAF2FF)
private val PresetPrimaryText = Color(0xFF123A73)
private val PresetSecondaryText = Color(0xFF6E85A3)
private val PresetDivider = Color(0xFFDCE5F0)
private val PresetSwitchCheckedTrack = Color(0xFF2E6CEB)
private val PresetSwitchUncheckedTrack = Color(0xFFD6E0EC)

@Composable
fun TaskPresetScreen(
    onBackClick: () -> Unit,
    onPresetAddClick: () -> Unit,
    onPresetDetailClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val viewModel: TaskPresetViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var guideVisible by remember { mutableStateOf(false) }

    if (appInfoVisible) {
        PresetPopupFrame(
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

    if (guideVisible) {
        PresetPopupFrame(
            title = stringResource(R.string.home_guide_title),
            confirmText = stringResource(R.string.home_guide_close),
            onDismiss = { guideVisible = false }
        ) {
            PopupGuideLine(text = stringResource(R.string.task_preset_top_title))
            PopupGuideLine(text = stringResource(R.string.task_preset_top_add))
            PopupGuideLine(text = stringResource(R.string.task_preset_top_name_hint))
            PopupGuideLine(text = stringResource(R.string.task_preset_top_name_example))
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = PresetBackground,
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
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_close),
                        tint = PresetPrimaryText
                    )
                }

                Text(
                    text = stringResource(R.string.task_preset_top_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PresetPrimaryText
                )

                Box {
                    TextButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.common_home),
                            tint = PresetPrimaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(PresetCardColor)
                    ) {
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_home)) },
                            onClick = {
                                menuExpanded = false
                                onHomeClick()
                            }
                        )
                        HorizontalDivider(color = PresetDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_kor)) },
                            onClick = {
                                menuExpanded = false
                                onKorClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_eng)) },
                            onClick = {
                                menuExpanded = false
                                onEngClick()
                            }
                        )
                        HorizontalDivider(color = PresetDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_app_info)) },
                            onClick = {
                                menuExpanded = false
                                appInfoVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_guide)) },
                            onClick = {
                                menuExpanded = false
                                guideVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_contact)) },
                            onClick = {
                                menuExpanded = false
                                onContactClick()
                            }
                        )
                        HorizontalDivider(color = PresetDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_exit)) },
                            onClick = {
                                menuExpanded = false
                                onExitClick()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PresetBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddPresetCard(
                    onClick = onPresetAddClick
                )
            }

            if (uiState.presetGroups.isEmpty()) {
                item {
                    EmptyStateCard(
                        text = stringResource(R.string.task_preset_top_empty)
                    )
                }
            } else {
                items(uiState.presetGroups) { preset ->
                    PresetRowCard(
                        preset = preset,
                        onClick = { onPresetDetailClick(preset.id) },
                        onToggle = { checked ->
                            viewModel.onPresetToggle(preset.id, checked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuText(
    text: String
) {
    Text(
        text = text,
        color = PresetPrimaryText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun AddPresetCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = PresetAccentSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(PresetPrimaryText, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.task_preset_top_add),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = stringResource(R.string.task_preset_top_add),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = PresetPrimaryText,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun PresetRowCard(
    preset: TaskPresetGroupItem,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = PresetCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizeText(
                text = preset.name,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PresetPrimaryText,
                    textAlign = TextAlign.Start
                ),
                maxLines = 2,
                minFontSize = 14.sp,
                textAlign = TextAlign.Start
            )

            Switch(
                checked = preset.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PresetSwitchCheckedTrack,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = PresetSwitchUncheckedTrack
                )
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = PresetCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = PresetSecondaryText,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
        )
    }
}

@Composable
private fun PresetPopupFrame(
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
            colors = CardDefaults.cardColors(containerColor = PresetCardColor),
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
                    color = PresetPrimaryText
                )

                HorizontalDivider(color = PresetDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = PresetDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = PresetPrimaryText,
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
            color = PresetSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = PresetPrimaryText,
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
        color = PresetPrimaryText
    )
}