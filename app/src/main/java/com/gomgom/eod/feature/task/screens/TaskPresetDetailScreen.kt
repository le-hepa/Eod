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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.gomgom.eod.core.common.AutoResizeText
import com.gomgom.eod.feature.task.viewmodel.CycleUnit
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkViewModel

private val LowerBackground = Color(0xFFF5F8FC)
private val LowerCardColor = Color.White
private val LowerPrimaryText = Color(0xFF123A73)
private val LowerSecondaryText = Color(0xFF6E85A3)
private val LowerDivider = Color(0xFFDCE5F0)

@Composable
fun TaskPresetDetailScreen(
    presetId: Long,
    onBackClick: () -> Unit,
    onWorkAddClick: () -> Unit,
    onWorkDetailClick: (Long, Long) -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val workViewModel: TaskPresetWorkViewModel = viewModel()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()
    val works by workViewModel.worksForPreset(presetId).collectAsState(initial = emptyList())

    val presetName = presetGroups.firstOrNull { it.id == presetId }?.name ?: ""
    var searchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }

    val filteredWorks = works.filter {
        searchText.isBlank() || it.name.contains(searchText, ignoreCase = true)
    }

    if (appInfoVisible) {
        LowerPopupFrame(
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
        containerColor = LowerBackground,
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
                            tint = LowerPrimaryText,
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
                            color = LowerPrimaryText,
                            textAlign = TextAlign.Center
                        )
                    }

                    TaskHamburgerMenuButton(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it },
                        iconTint = LowerPrimaryText,
                        menuBackgroundColor = LowerCardColor,
                        dividerColor = LowerDivider,
                        textColor = LowerPrimaryText,
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
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { searchExpanded = !searchExpanded },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.task_preset_lower_search_hint),
                            tint = LowerPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    TextButton(
                        onClick = onWorkAddClick,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.task_preset_lower_add_icon_desc),
                            tint = LowerPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                if (searchExpanded) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = taskOutlinedTextFieldColors(LowerPrimaryText, LowerSecondaryText),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.task_preset_lower_search_hint),
                                color = LowerSecondaryText
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LowerBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredWorks.isEmpty()) {
                item {
                    EmptyWorkCard(
                        text = stringResource(R.string.task_preset_lower_empty)
                    )
                }
            } else {
                items(filteredWorks) { work ->
                    WorkRowCard(
                        item = work,
                        onClick = { onWorkDetailClick(presetId, work.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkRowCard(
    item: TaskPresetWorkItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LowerCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizeText(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LowerPrimaryText,
                    textAlign = TextAlign.Start
                ),
                maxLines = 2,
                minFontSize = 14.sp,
                textAlign = TextAlign.Start
            )

            Text(
                text = "${item.cycleNumber} ${toCycleShort(item.cycleUnit)}",
                fontSize = 16.sp,
                color = LowerPrimaryText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun EmptyWorkCard(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LowerCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = LowerSecondaryText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun toCycleShort(unit: CycleUnit): String {
    return when (unit) {
        CycleUnit.DAY -> stringResource(R.string.cycle_unit_day_short)
        CycleUnit.WEEK -> stringResource(R.string.cycle_unit_week_short)
        CycleUnit.MONTH -> stringResource(R.string.cycle_unit_month_short)
        CycleUnit.YEAR -> stringResource(R.string.cycle_unit_year_short)
    }
}

@Composable
private fun LowerPopupFrame(
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
            colors = CardDefaults.cardColors(containerColor = LowerCardColor),
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
                    color = LowerPrimaryText
                )

                HorizontalDivider(color = LowerDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = LowerDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = LowerPrimaryText,
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
            color = LowerSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = LowerPrimaryText,
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
        color = LowerPrimaryText
    )
}
