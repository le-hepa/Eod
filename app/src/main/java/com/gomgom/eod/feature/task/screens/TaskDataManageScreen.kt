package com.gomgom.eod.feature.task.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.R
import com.gomgom.eod.feature.task.viewmodel.TaskDataManageViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskPresetGroupItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DataManageBackground = Color(0xFFF5F8FC)
private val DataManageCardColor = Color.White
private val DataManagePrimaryText = Color(0xFF123A73)
private val DataManageSecondaryText = Color(0xFF6E85A3)
private val DataManageDivider = Color(0xFFDCE5F0)
private val DataManageDangerText = Color(0xFF8B1E1E)
private val DataManageSelection = Color(0xFFE8F1FF)

private enum class DataManageAction(
    val titleResId: Int,
    val descriptionResId: Int
) {
    IMPORT_PRESET(
        R.string.task_data_manage_import_title,
        R.string.task_data_manage_import_desc
    ),
    EXPORT_PRESET(
        R.string.task_data_manage_export_title,
        R.string.task_data_manage_export_desc
    ),
    RESET_PRESET(
        R.string.task_data_manage_reset_preset_title,
        R.string.task_data_manage_reset_preset_desc
    ),
    RESET_RECORD(
        R.string.task_data_manage_reset_record_title,
        R.string.task_data_manage_reset_record_desc
    ),
    RESET_APP(
        R.string.task_data_manage_reset_app_title,
        R.string.task_data_manage_reset_app_desc
    )
}

@Composable
fun TaskDataManageScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val viewModel: TaskDataManageViewModel = viewModel()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<DataManageAction?>(null) }
    var exportSelectionVisible by remember { mutableStateOf(false) }
    var appResetConfirmVisible by remember { mutableStateOf(false) }
    var selectedExportPresetIds by remember { mutableStateOf(setOf<Long>()) }

    val exportDoneText = stringResource(R.string.task_data_manage_export_done)
    val importDoneText = stringResource(R.string.task_data_manage_import_done)
    val importFailText = stringResource(R.string.task_data_manage_import_failed)
    val resetPresetDoneText = stringResource(R.string.task_data_manage_reset_preset_done)
    val resetRecordDoneText = stringResource(R.string.task_data_manage_reset_record_done)
    val resetAppDoneText = stringResource(R.string.task_data_manage_reset_app_done)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        scope.launch {
            val json = withContext(Dispatchers.IO) {
                viewModel.exportPresetsJson(selectedExportPresetIds)
            }
            if (uri != null && json != null) {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                Toast.makeText(context, exportDoneText, Toast.LENGTH_SHORT).show()
            }
            exportSelectionVisible = false
            pendingAction = null
            selectedExportPresetIds = emptySet()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                uri?.let { targetUri ->
                    context.contentResolver.openInputStream(targetUri)?.use { stream ->
                        viewModel.importPresetsJson(stream.bufferedReader(Charsets.UTF_8).readText())
                    }
                } == true
            }
            Toast.makeText(
                context,
                if (imported) importDoneText else importFailText,
                Toast.LENGTH_SHORT
            ).show()
            pendingAction = null
        }
    }

    if (appInfoVisible) {
        DataManagePopupFrame(
            title = stringResource(R.string.home_app_info_title),
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

    pendingAction?.let { action ->
        DataManageActionDialog(
            action = action,
            onDismiss = {
                pendingAction = null
                selectedExportPresetIds = emptySet()
            },
            onConfirm = {
                when (action) {
                    DataManageAction.IMPORT_PRESET -> importLauncher.launch(arrayOf("application/json"))
                    DataManageAction.EXPORT_PRESET -> exportSelectionVisible = true
                    DataManageAction.RESET_PRESET -> {
                        viewModel.clearPresets()
                        Toast.makeText(context, resetPresetDoneText, Toast.LENGTH_SHORT).show()
                        pendingAction = null
                    }
                    DataManageAction.RESET_RECORD -> {
                        viewModel.clearWorkRecords()
                        Toast.makeText(context, resetRecordDoneText, Toast.LENGTH_SHORT).show()
                        pendingAction = null
                    }
                    DataManageAction.RESET_APP -> appResetConfirmVisible = true
                }
            }
        )
    }

    if (exportSelectionVisible) {
        ExportPresetSelectionDialog(
            presets = presetGroups,
            selectedIds = selectedExportPresetIds,
            onToggle = { presetId ->
                selectedExportPresetIds = if (presetId in selectedExportPresetIds) {
                    selectedExportPresetIds - presetId
                } else {
                    selectedExportPresetIds + presetId
                }
            },
            onDismiss = {
                exportSelectionVisible = false
                pendingAction = null
                selectedExportPresetIds = emptySet()
            },
            onConfirm = { exportLauncher.launch(viewModel.exportFileName(selectedExportPresetIds)) }
        )
    }

    if (appResetConfirmVisible) {
        AlertDialog(
            onDismissRequest = { appResetConfirmVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTaskApp()
                        appResetConfirmVisible = false
                        pendingAction = null
                        Toast.makeText(context, resetAppDoneText, Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = DataManageDangerText,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { appResetConfirmVisible = false }) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = DataManagePrimaryText
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.task_data_manage_reset_app_confirm_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.task_data_manage_reset_app_confirm_body),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            containerColor = DataManageCardColor
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DataManageBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_close),
                        tint = DataManagePrimaryText,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.task_data_manage_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DataManagePrimaryText,
                        textAlign = TextAlign.Center
                    )
                }
                TaskHamburgerMenuButton(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    iconTint = DataManagePrimaryText,
                    menuBackgroundColor = DataManageCardColor,
                    dividerColor = DataManageDivider,
                    textColor = DataManagePrimaryText,
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
                .background(DataManageBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(DataManageAction.entries) { action ->
                DataManageActionCard(
                    title = stringResource(action.titleResId),
                    onClick = { pendingAction = action }
                )
            }
        }
    }
}

@Composable
private fun DataManageActionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DataManageCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = DataManagePrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DataManageActionDialog(
    action: DataManageAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DataManagePopupFrame(
        title = stringResource(action.titleResId),
        onDismiss = onDismiss,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = DataManageSecondaryText
                    )
                }
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.task_data_manage_run),
                        color = DataManagePrimaryText
                    )
                }
            }
        }
    ) {
        Text(
            text = stringResource(action.descriptionResId),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = DataManagePrimaryText,
            fontSize = 15.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun ExportPresetSelectionDialog(
    presets: List<TaskPresetGroupItem>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DataManagePopupFrame(
        title = stringResource(R.string.task_data_manage_export_title),
        onDismiss = onDismiss,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = DataManageSecondaryText
                    )
                }
                TextButton(
                    onClick = onConfirm,
                    enabled = selectedIds.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.task_data_manage_run),
                        color = if (selectedIds.isNotEmpty()) DataManagePrimaryText else DataManageSecondaryText
                    )
                }
            }
        }
    ) {
        Text(
            text = stringResource(R.string.task_data_manage_export_select),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = DataManagePrimaryText,
            fontSize = 15.sp
        )
        if (presets.isEmpty()) {
            Text(
                text = stringResource(R.string.task_data_manage_no_preset),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = DataManageSecondaryText,
                fontSize = 14.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(preset.id) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (preset.id in selectedIds) DataManageSelection else DataManageCardColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.name,
                                modifier = Modifier.weight(1f),
                                color = DataManagePrimaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Checkbox(
                                checked = preset.id in selectedIds,
                                onCheckedChange = { onToggle(preset.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataManagePopupFrame(
    title: String,
    onDismiss: () -> Unit,
    footer: @Composable (() -> Unit)? = null,
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
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DataManageCardColor),
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
                    color = DataManagePrimaryText,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(color = DataManageDivider)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    content()
                }
                HorizontalDivider(color = DataManageDivider)
                if (footer != null) {
                    footer()
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.common_close),
                                color = DataManagePrimaryText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = DataManageSecondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = DataManagePrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
