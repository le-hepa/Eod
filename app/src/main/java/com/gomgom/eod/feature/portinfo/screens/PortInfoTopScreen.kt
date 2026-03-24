package com.gomgom.eod.feature.portinfo.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.gomgom.eod.R
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeEntry
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolDbState
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortEditorFocusField
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchDisplayMode
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchResultItem
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchSourceField
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolTempState
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolUiState
import com.gomgom.eod.feature.task.screens.TaskHamburgerMenuButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.OutputStreamWriter
import android.media.MediaMetadataRetriever
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PortInfoTopScreen(
    screenState: PortScreenState,
    currentSource: PortToolSource,
    listItems: List<PortRecordListItem>,
    canCreate: Boolean,
    canEditRecord: Boolean,
    uiState: PortToolUiState,
    dbState: PortToolDbState,
    tempState: PortToolTempState,
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit,
    onSaveClick: () -> Unit,
    onEnterSearch: () -> Unit,
    onEnterList: () -> Unit,
    onEnterEdit: () -> Unit,
    onSaveComplete: () -> Unit,
    onDeleteCurrent: () -> Unit,
    onDiscardAndGoSearch: () -> Unit,
    onDiscardAndGoBack: () -> Unit,
    onToggleVesselReporting: () -> Unit,
    onToggleAnchorage: () -> Unit,
    onToggleBerth: () -> Unit,
    onBundleChange: (PortRecordBundle) -> Unit,
    onEditorFieldFocusChange: (PortEditorFocusField, Boolean) -> Unit,
    onFieldSearchInputChange: (PortSearchSourceField, String) -> Unit,
    onFormFieldSearchInputChange: (String, String) -> Unit,
    onSearchResultSelect: (PortSearchResultItem) -> Unit,
    onAttachmentsSelected: (List<PortAttachmentEntity>) -> Unit,
    onAttachmentDelete: (Long) -> Unit,
    onSourceSelect: (PortToolSource) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchDisplayModeChange: (PortSearchDisplayMode) -> Unit,
    onSearchModeChange: () -> Unit,
    listCountryFilter: String,
    listPortFilter: String,
    listCodeFilter: String,
    onListCountryFilterChange: (String) -> Unit,
    onListPortFilterChange: (String) -> Unit,
    onListCodeFilterChange: (String) -> Unit,
    onExportJson: ((String) -> Unit) -> Unit,
    onExportCsv: ((String) -> Unit) -> Unit,
    onImportJson: (String, (Result<Unit>) -> Unit) -> Unit,
    onLiveSearchConfirm: () -> Unit,
    onLiveSearchCancel: () -> Unit,
    onLiveSearchHeaderDismiss: () -> Unit,
    hasEditableChanges: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDataManage by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showSharedSourceInfo by remember { mutableStateOf(false) }
    var currentSourceInfoMessage by remember { mutableStateOf<String?>(null) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var attachmentError by remember { mutableStateOf<String?>(null) }
    var showDiscardSearchConfirm by remember { mutableStateOf(false) }
    var showDiscardBackConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    var switchingSourceTo by remember { mutableStateOf<PortToolSource?>(null) }
    val sourceCanEdit = currentSource == PortToolSource.LOCAL
    val versionLabel = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    val displayResults by remember(uiState.searchResults) { derivedStateOf { uiState.searchResults } }
    val activeBundle by remember(tempState.editorBundle, dbState.selectedRecord) {
        derivedStateOf { tempState.editorBundle ?: dbState.selectedRecord }
    }
    val searchPlaceholder by remember(screenState, uiState.searchMode, context) {
        derivedStateOf {
            if (screenState == PortScreenState.LIST) {
                context.getString(R.string.port_info_search_placeholder_record_mode)
            } else if (uiState.searchMode == com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSearchMode.COUNTRY_PORT) {
                context.getString(R.string.port_info_search_placeholder_unlocode_mode)
            } else {
                context.getString(R.string.port_info_search_placeholder_record_mode)
            }
        }
    }
    val scrollState = rememberScrollState()
    val searchFocusRequester = remember { FocusRequester() }
    var isTopSearchFocused by remember { mutableStateOf(false) }
    var hideLiveSearchHeaderAtTop by remember(uiState.isLiveSearchHeaderVisible) { mutableStateOf(false) }
    var liveSearchHeaderScrolledAfterShow by remember(uiState.isLiveSearchHeaderVisible) { mutableStateOf(false) }
    val resultFloatThreshold = 120
    val isScrollAtTop by remember(scrollState) {
        derivedStateOf { scrollState.value == 0 }
    }
    val targetSearchDisplayMode by remember(scrollState) {
        derivedStateOf {
            if (scrollState.value < resultFloatThreshold) PortSearchDisplayMode.PUSH
            else PortSearchDisplayMode.FLOAT
        }
    }
    val showLiveSearchHeader by remember(
        uiState.isLiveSearchHeaderVisible,
        uiState.isLiveSearchEnabled,
        uiState.pendingLiveSearchCount,
        hideLiveSearchHeaderAtTop
    ) {
        derivedStateOf {
            uiState.isLiveSearchHeaderVisible &&
                !uiState.isLiveSearchEnabled &&
                uiState.pendingLiveSearchCount > 0 &&
                !hideLiveSearchHeaderAtTop
        }
    }
    val showAttachmentOptionsDialog by remember(showAttachmentOptions) {
        derivedStateOf { showAttachmentOptions }
    }
    val showDataManageDialog by remember(showDataManage) {
        derivedStateOf { showDataManage }
    }
    val showSaveConfirmDialog by remember(showSaveConfirm) {
        derivedStateOf { showSaveConfirm }
    }
    val showSharedSourceInfoDialog by remember(showSharedSourceInfo) {
        derivedStateOf { showSharedSourceInfo }
    }
    val showCurrentSourceInfoDialog by remember(currentSourceInfoMessage) {
        derivedStateOf { currentSourceInfoMessage != null }
    }
    val currentAttachmentError by remember(attachmentError) {
        derivedStateOf { attachmentError }
    }
    val showSwitchingSourceDialog by remember(switchingSourceTo) {
        derivedStateOf { switchingSourceTo != null }
    }
    val showPushResults by remember(uiState.isSearchResultVisible, uiState.searchDisplayMode, displayResults) {
        derivedStateOf {
            uiState.isSearchResultVisible &&
                uiState.searchDisplayMode == PortSearchDisplayMode.PUSH &&
                displayResults.isNotEmpty()
        }
    }
    val showFloatResults by remember(uiState.isSearchResultVisible, uiState.searchDisplayMode, displayResults) {
        derivedStateOf {
            uiState.isSearchResultVisible &&
                uiState.searchDisplayMode == PortSearchDisplayMode.FLOAT &&
                displayResults.isNotEmpty()
        }
    }
    val searchInputsEnabled by remember(screenState) {
        derivedStateOf { screenState != PortScreenState.RECORD }
    }
    val recordBodyEditable by remember(screenState, sourceCanEdit) {
        derivedStateOf {
            when (screenState) {
                PortScreenState.SEARCH -> true
                PortScreenState.CREATE -> sourceCanEdit
                PortScreenState.EDIT -> sourceCanEdit
                else -> false
            }
        }
    }
    val showResultPanels by remember(screenState) {
        derivedStateOf { screenState == PortScreenState.SEARCH || screenState == PortScreenState.CREATE || screenState == PortScreenState.EDIT }
    }
    val stateLabel by remember(screenState, currentSource) {
        derivedStateOf {
            when (screenState) {
                PortScreenState.LIST -> if (currentSource == PortToolSource.LOCAL) "리스트" else "공유기록 리스트"
                PortScreenState.SEARCH -> if (currentSource == PortToolSource.LOCAL) "검색" else "공유기록 검색"
                PortScreenState.CREATE -> "기록작성"
                PortScreenState.RECORD -> if (currentSource == PortToolSource.LOCAL) "내 기록" else "공유 기록"
                PortScreenState.EDIT -> "기록편집"
            }
        }
    }

    LaunchedEffect(uiState.isLiveSearchHeaderVisible, isScrollAtTop) {
        if (!uiState.isLiveSearchHeaderVisible) {
            hideLiveSearchHeaderAtTop = false
            liveSearchHeaderScrolledAfterShow = false
        } else if (!isScrollAtTop) {
            liveSearchHeaderScrolledAfterShow = true
            hideLiveSearchHeaderAtTop = false
        } else if (liveSearchHeaderScrolledAfterShow) {
            hideLiveSearchHeaderAtTop = true
        }
    }

    LaunchedEffect(uiState.searchSourceField, uiState.isSearchResultVisible, targetSearchDisplayMode) {
        if (!uiState.isSearchResultVisible) return@LaunchedEffect
        onSearchDisplayModeChange(targetSearchDisplayMode)
    }

    LaunchedEffect(currentSource) {
        if (switchingSourceTo == currentSource) {
            switchingSourceTo = null
            if (currentSource == PortToolSource.SHARED) {
                showSharedSourceInfo = true
            }
        }
    }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onExportJson { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    OutputStreamWriter(output).use { writer -> writer.write(json) }
                }
            }.onSuccess {
                Toast.makeText(context, context.getString(R.string.port_info_json_exported), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, context.getString(R.string.port_info_json_export_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onExportCsv { csv ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    OutputStreamWriter(output).use { writer -> writer.write(csv) }
                }
            }.onSuccess {
                Toast.makeText(context, context.getString(R.string.port_info_csv_exported), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, context.getString(R.string.port_info_csv_export_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(input.reader()).use { it.readText() }
            }.orEmpty()
        }.onSuccess { json ->
            onImportJson(json) { result ->
                result.onSuccess {
                    Toast.makeText(context, context.getString(R.string.port_info_json_imported), Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, context.getString(R.string.port_info_json_import_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }.onFailure {
            Toast.makeText(context, context.getString(R.string.port_info_json_import_failed), Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        val currentBundle = activeBundle
        val recordId = currentBundle?.record?.id ?: run {
            pendingPhotoUri = null
            pendingVideoUri = null
            return@rememberLauncherForActivityResult
        }
        val existing = currentBundle.attachments
        val capturedUris = listOfNotNull(pendingPhotoUri, pendingVideoUri)
            .filter { uri -> context.contentResolver.openInputStream(uri)?.use { true } == true }
        pendingPhotoUri = null
        pendingVideoUri = null
        if (capturedUris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val processed = buildAttachmentPayload(context, recordId, existing, capturedUris)
            processed.fold(
                onSuccess = { attachments ->
                    if (attachments.isNotEmpty()) onAttachmentsSelected(attachments)
                },
                onFailure = {
                    attachmentError = it.message ?: context.getString(R.string.port_info_attachment_error)
                }
            )
        }
    }

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val currentBundle = activeBundle ?: return@rememberLauncherForActivityResult
        val recordId = currentBundle.record.id
        val existing = currentBundle.attachments
        scope.launch {
            val processed = buildAttachmentPayload(context, recordId, existing, uris)
            processed.fold(
                onSuccess = { attachments ->
                    if (attachments.isNotEmpty()) onAttachmentsSelected(attachments)
                },
                onFailure = {
                    attachmentError = it.message ?: context.getString(R.string.port_info_attachment_error)
                }
            )
        }
    }

    if (showAttachmentOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentOptions = false },
            title = {
                Text(
                    stringResource(R.string.port_info_add_attachment),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DataManageActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.PhotoCamera,
                        label = stringResource(R.string.port_info_attachment_camera)
                    ) {
                        showAttachmentOptions = false
                        val tempUris = createCameraOutputUris(context)
                        pendingPhotoUri = tempUris.first
                        pendingVideoUri = tempUris.second
                        val chooser = createCameraChooserIntent(context, tempUris.first, tempUris.second)
                        if (chooser != null) {
                            cameraLauncher.launch(chooser)
                        } else {
                            attachmentError = context.getString(R.string.port_info_attachment_error)
                        }
                    }
                    DataManageActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.AttachFile,
                        label = stringResource(R.string.port_info_attachment_file_add)
                    ) {
                        showAttachmentOptions = false
                        attachmentLauncher.launch(arrayOf("image/*", "video/*", "application/pdf", "*/*"))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentOptions = false }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }

    if (showDataManageDialog) {
        AlertDialog(
            onDismissRequest = { showDataManage = false },
            title = {
                Text(
                    stringResource(R.string.port_info_data_manage),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DataManageActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Inventory2,
                            label = stringResource(R.string.port_info_my_records),
                            selected = currentSource == PortToolSource.LOCAL,
                            onClick = {
                                if (currentSource == PortToolSource.LOCAL) {
                                    currentSourceInfoMessage = context.getString(R.string.port_info_current_my_records)
                                } else {
                                    switchingSourceTo = PortToolSource.LOCAL
                                    onSourceSelect(PortToolSource.LOCAL)
                                }
                                showDataManage = false
                            }
                        )
                        DataManageActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.FolderShared,
                            label = stringResource(R.string.port_info_shared_records),
                            selected = currentSource == PortToolSource.SHARED,
                            onClick = {
                                if (currentSource == PortToolSource.SHARED) {
                                    currentSourceInfoMessage = context.getString(R.string.port_info_current_shared_records)
                                } else {
                                    switchingSourceTo = PortToolSource.SHARED
                                    onSourceSelect(PortToolSource.SHARED)
                                }
                                showDataManage = false
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DataManageActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Download,
                            label = stringResource(R.string.port_info_import),
                            onClick = {
                                showDataManage = false
                                importJsonLauncher.launch(arrayOf("application/json"))
                            }
                        )
                        DataManageActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.SaveAlt,
                            label = stringResource(R.string.port_info_export_json),
                            onClick = {
                                showDataManage = false
                                createJsonLauncher.launch("port_records.json")
                            }
                        )
                    }
                    DataManageActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.SaveAlt,
                        label = stringResource(R.string.port_info_export_csv),
                        onClick = {
                            showDataManage = false
                            createCsvLauncher.launch("port_records.csv")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataManage = false }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text(stringResource(R.string.port_info_save_title), color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.port_info_save_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSaveConfirm = false
                    onSaveClick()
                    onSaveComplete()
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.port_info_attachment_delete_title),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.port_info_record_delete_message),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteCurrent()
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showDiscardSearchConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardSearchConfirm = false },
            title = { Text(stringResource(R.string.port_info_discard_changes_title), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.port_info_discard_changes_message), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardSearchConfirm = false
                    onDiscardAndGoSearch()
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardSearchConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showDiscardBackConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardBackConfirm = false },
            title = { Text(stringResource(R.string.port_info_discard_changes_title), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.port_info_discard_changes_message), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardBackConfirm = false
                    onDiscardAndGoBack()
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardBackConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showSharedSourceInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSharedSourceInfo = false },
            title = {
                Text(
                    text = stringResource(R.string.port_info_shared_notice_title),
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.port_info_shared_notice_message)
                )
            },
            confirmButton = {
                TextButton(onClick = { showSharedSourceInfo = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }

    if (showCurrentSourceInfoDialog) {
        Dialog(onDismissRequest = { currentSourceInfoMessage = null }) {
            androidx.compose.material3.Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.port_info_current_source_title),
                        color = Color(0xFF123A73),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Text(
                        text = currentSourceInfoMessage.orEmpty(),
                        color = Color(0xFF33445E),
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    TextButton(onClick = { currentSourceInfoMessage = null }) {
                        Text(stringResource(R.string.common_ok))
                    }
                }
            }
        }
    }

    if (currentAttachmentError != null) {
        AlertDialog(
            onDismissRequest = { attachmentError = null },
            title = { Text(stringResource(R.string.port_info_attachment_error_title), color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(currentAttachmentError.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { attachmentError = null }) { Text(stringResource(R.string.common_ok)) }
            }
        )
    }

    if (showSwitchingSourceDialog) {
        Dialog(onDismissRequest = {}) {
            androidx.compose.material3.Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF3F5F93)
                    )
                    Text(
                        text = stringResource(R.string.port_info_switching_source),
                        color = Color(0xFF123A73),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F8FC))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if ((screenState == PortScreenState.CREATE || screenState == PortScreenState.EDIT) && hasEditableChanges) {
                            showDiscardBackConfirm = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.port_info_back),
                            tint = Color(0xFF123A73)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.port_info_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TaskHamburgerMenuButton(
                        expanded = showMenu,
                        onExpandedChange = { showMenu = it },
                        iconTint = Color(0xFF123A73),
                        menuBackgroundColor = Color.White,
                        dividerColor = Color(0xFFE3ECF8),
                        textColor = Color(0xFF123A73),
                        onHomeClick = onBackClick,
                        onKorClick = {
                            AppLanguageManager.applyKor(context)
                            (context as? Activity)?.recreate()
                        },
                        onEngClick = {
                            AppLanguageManager.applyEng(context)
                            (context as? Activity)?.recreate()
                        },
                        onAppInfoClick = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.port_info_version_toast, versionLabel),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onGuideClick = {},
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.home_contact_mail_subject))
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "App Version: $versionLabel\nAndroid Version: ${Build.VERSION.RELEASE}\nDevice Model: ${Build.MANUFACTURER} ${Build.MODEL}\n${context.getString(R.string.port_info_contact_body)}\n"
                                )
                            }
                            runCatching { context.startActivity(intent) }
                        },
                        onExitClick = { (context as? Activity)?.finish() }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            if (screenState == PortScreenState.LIST) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SearchSection(
                        searchEnabled = searchInputsEnabled,
                        canSave = hasEditableChanges,
                    stateLabel = stateLabel,
                    topSearchQuery = uiState.topSearchQuery,
                    searchPlaceholder = searchPlaceholder,
                    showFilterButton = screenState != PortScreenState.LIST,
                    searchMode = uiState.searchMode,
                        pendingLiveSearchCount = uiState.pendingLiveSearchCount,
                        showLiveSearchHeader = showLiveSearchHeader,
                        isTopSearchFocused = isTopSearchFocused,
                        searchFocusRequester = searchFocusRequester,
                        showFilterMenu = showFilterMenu,
                        onTopSearchFocusChanged = { isTopSearchFocused = it },
                        onTopSearchQueryChange = onSearchQueryChange,
                        onFilterMenuExpandedChange = { showFilterMenu = it },
                        onSearchModeToggle = onSearchModeChange,
                        onDataManageClick = { showDataManage = true },
                        leftAction = PortHeaderAction(
                            icon = PortHeaderIcons.Search,
                            contentDescription = stringResource(R.string.port_info_search),
                            onClick = onEnterSearch
                        ),
                        rightActions = if (canCreate) {
                            listOf(
                                PortHeaderAction(
                                    icon = PortHeaderIcons.New,
                                    contentDescription = stringResource(R.string.port_info_add_record),
                                    onClick = onAddRecordClick
                                )
                            )
                        } else emptyList(),
                        onLiveSearchConfirm = onLiveSearchConfirm,
                        onLiveSearchHeaderDismiss = onLiveSearchHeaderDismiss,
                        onSearchBarClick = { searchFocusRequester.requestFocus() }
                    )

                    PortSearchCardSection(
                        editable = true,
                        countryValue = listCountryFilter,
                        portValue = listPortFilter,
                        unlocodeValue = listCodeFilter,
                        onEditorFieldFocusChange = onEditorFieldFocusChange,
                        onCountryChange = onListCountryFilterChange,
                        onPortChange = onListPortFilterChange,
                        onUnlocodeChange = onListCodeFilterChange
                    )

                    RecordListSection(
                        modifier = Modifier.weight(1f),
                        items = listItems,
                        onRecordClick = onRecordClick
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SearchSection(
                        searchEnabled = searchInputsEnabled,
                        canSave = hasEditableChanges,
                    stateLabel = stateLabel,
                    topSearchQuery = uiState.topSearchQuery,
                    searchPlaceholder = searchPlaceholder,
                    showFilterButton = screenState != PortScreenState.LIST,
                    searchMode = uiState.searchMode,
                        pendingLiveSearchCount = uiState.pendingLiveSearchCount,
                        showLiveSearchHeader = showLiveSearchHeader,
                        isTopSearchFocused = isTopSearchFocused,
                        searchFocusRequester = searchFocusRequester,
                        showFilterMenu = showFilterMenu,
                        onTopSearchFocusChanged = { isTopSearchFocused = it },
                        onTopSearchQueryChange = onSearchQueryChange,
                        onFilterMenuExpandedChange = { showFilterMenu = it },
                        onSearchModeToggle = onSearchModeChange,
                        onDataManageClick = { showDataManage = true },
                        leftAction = when (screenState) {
                            PortScreenState.SEARCH -> PortHeaderAction(
                                icon = PortHeaderIcons.List,
                                contentDescription = stringResource(R.string.port_info_record_list),
                                onClick = onEnterList
                            )
                            PortScreenState.CREATE,
                            PortScreenState.RECORD,
                            PortScreenState.EDIT -> PortHeaderAction(
                                icon = PortHeaderIcons.Search,
                                contentDescription = stringResource(R.string.port_info_search),
                                onClick = {
                                    if ((screenState == PortScreenState.CREATE || screenState == PortScreenState.EDIT) && hasEditableChanges) {
                                        showDiscardSearchConfirm = true
                                    } else {
                                        onEnterSearch()
                                    }
                                }
                            )
                            else -> PortHeaderAction(
                                icon = PortHeaderIcons.Search,
                                contentDescription = stringResource(R.string.port_info_search),
                                onClick = onEnterSearch
                            )
                        },
                        rightActions = when (screenState) {
                            PortScreenState.SEARCH -> if (canCreate) {
                                listOf(
                                    PortHeaderAction(
                                        icon = PortHeaderIcons.New,
                                        contentDescription = stringResource(R.string.port_info_add_record),
                                        onClick = onAddRecordClick
                                    )
                                )
                            } else emptyList()
                            PortScreenState.CREATE -> if (canCreate) {
                                listOf(
                                    PortHeaderAction(
                                        icon = PortHeaderIcons.Save,
                                        contentDescription = stringResource(R.string.port_info_save_record),
                                        enabled = hasEditableChanges,
                                        onClick = { showSaveConfirm = true }
                                    )
                                )
                            } else emptyList()
                            PortScreenState.RECORD -> if (canEditRecord) {
                                listOf(
                                    PortHeaderAction(
                                        icon = PortHeaderIcons.Edit,
                                        contentDescription = stringResource(R.string.port_info_edit_record),
                                        onClick = onEnterEdit
                                    )
                                )
                            } else {
                                listOf(
                                    PortHeaderAction(
                                        icon = PortHeaderIcons.Delete,
                                        contentDescription = stringResource(R.string.port_info_delete_record),
                                        onClick = { showDeleteConfirm = true }
                                    )
                                )
                            }
                            PortScreenState.EDIT -> listOf(
                                PortHeaderAction(
                                    icon = PortHeaderIcons.Save,
                                    contentDescription = stringResource(R.string.port_info_save_record),
                                    enabled = hasEditableChanges,
                                    onClick = { showSaveConfirm = true }
                                ),
                                PortHeaderAction(
                                    icon = PortHeaderIcons.Delete,
                                    contentDescription = stringResource(R.string.port_info_delete_record),
                                    onClick = { showDeleteConfirm = true }
                                )
                            )
                            else -> emptyList()
                        },
                        onLiveSearchConfirm = onLiveSearchConfirm,
                        onLiveSearchHeaderDismiss = onLiveSearchHeaderDismiss,
                        onSearchBarClick = { searchFocusRequester.requestFocus() }
                    )

                    if (showResultPanels) {
                        ResultPanelSection(
                            isVisible = showPushResults,
                            displayMode = PortSearchDisplayMode.PUSH,
                            results = displayResults,
                            query = uiState.searchQuery,
                            onClick = onSearchResultSelect
                        )
                    }

                    val currentBundle = activeBundle
                    if (currentBundle != null) {
                        RecordContentSection(
                            editable = recordBodyEditable,
                            searchCardEditable = searchInputsEnabled,
                            bundle = currentBundle,
                            isVesselReportingExpanded = tempState.isVesselReportingExpanded,
                            isAnchorageExpanded = tempState.isAnchorageExpanded,
                            isBerthExpanded = tempState.isBerthExpanded,
                            onToggleVesselReporting = onToggleVesselReporting,
                            onToggleAnchorage = onToggleAnchorage,
                            onToggleBerth = onToggleBerth,
                            onBundleChange = onBundleChange,
                            onEditorFieldFocusChange = onEditorFieldFocusChange,
                            onFieldSearchInputChange = onFieldSearchInputChange,
                            onFormFieldSearchInputChange = onFormFieldSearchInputChange,
                            onAddAttachmentClick = { showAttachmentOptions = true },
                            onDeleteAttachmentClick = onAttachmentDelete
                        )
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                if (showResultPanels) {
                    ResultPanelSection(
                        isVisible = showFloatResults,
                        displayMode = PortSearchDisplayMode.FLOAT,
                        results = displayResults,
                        query = uiState.searchQuery,
                        onClick = onSearchResultSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun DataManageActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFEAF2FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color(0xFF3F5F93) else Color(0xFF5D7598),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = Color(0xFF123A73),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

private suspend fun buildAttachmentPayload(
    context: android.content.Context,
    recordId: Long,
    existing: List<PortAttachmentEntity>,
    uris: List<Uri>
): Result<List<PortAttachmentEntity>> = withContext(Dispatchers.IO) {
    runCatching {
        if (uris.isEmpty()) return@runCatching emptyList()
        val resolver = context.contentResolver
        val existingImageCount = existing.count { it.attachmentType == PortToolType.IMAGE }
        val existingVideoCount = existing.count { it.attachmentType == PortToolType.VIDEO }
        val existingFileCount = existing.count { it.attachmentType == PortToolType.FILE }
        val existingSize = existing.sumOf { it.fileSize }
        val maxTotalSize = 10L * 1024 * 1024
        var imageCount = existingImageCount
        var videoCount = existingVideoCount
        var fileCount = existingFileCount
        var totalSize = existingSize
        val attachments = mutableListOf<PortAttachmentEntity>()
        val now = System.currentTimeMillis()

        uris.forEachIndexed { index, uri ->
            runCatching {
                if (uri.scheme == android.content.ContentResolver.SCHEME_CONTENT) {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            val metadata = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        Pair(
                            if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else "",
                            if (sizeIndex >= 0) cursor.getLong(sizeIndex).coerceAtLeast(0L) else 0L
                        )
                    } else {
                        "" to 0L
                    }
                } ?: ("" to 0L)
            val displayName = metadata.first
            val mimeType = resolver.getType(uri).orEmpty()
            val attachmentType = resolveAttachmentType(mimeType, displayName)
            val fileSize = metadata.second.takeIf { it > 0L }
                ?: resolver.openAssetFileDescriptor(uri, "r")?.use { afd -> afd.length.coerceAtLeast(0L) }
                ?: 0L
            val projectedTotalSize = totalSize + fileSize
            if (projectedTotalSize > maxTotalSize) {
                error(context.getString(R.string.port_info_attachment_limit_total))
            }
            when (attachmentType) {
                PortToolType.IMAGE -> {
                    imageCount++
                    if (imageCount > 20) error(context.getString(R.string.port_info_attachment_limit_image))
                }
                PortToolType.VIDEO -> {
                    videoCount++
                    if (videoCount > 1) error(context.getString(R.string.port_info_attachment_limit_video_count))
                    val durationMs = MediaMetadataRetriever().use { retriever ->
                        retriever.setDataSource(context, uri)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    }
                    if (durationMs > 60_000L) error(context.getString(R.string.port_info_attachment_limit_video_duration))
                }
                else -> {
                    fileCount++
                    if (fileCount > 10) error(context.getString(R.string.port_info_attachment_limit_file))
                }
            }
            totalSize = projectedTotalSize
            attachments += PortAttachmentEntity(
                id = now + index,
                recordId = recordId,
                attachmentType = attachmentType,
                displayName = displayName,
                filePath = uri.toString(),
                mimeType = mimeType,
                fileSize = fileSize,
                thumbnailPath = if (attachmentType == PortToolType.FILE) "" else uri.toString(),
                normalizedText = displayName.lowercase(),
                createdAt = now,
                updatedAt = now
            )
        }
        attachments
    }
}

private inline fun <T> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> T): T {
    return try {
        block(this)
    } finally {
        runCatching { release() }
    }
}

private fun resolveAttachmentType(mimeType: String, displayName: String): String {
    val normalizedMime = mimeType.lowercase()
    val normalizedName = displayName.lowercase()
    return when {
        normalizedMime.startsWith("image/") -> PortToolType.IMAGE
        normalizedMime.startsWith("video/") -> PortToolType.VIDEO
        normalizedName.endsWith(".jpg") || normalizedName.endsWith(".jpeg") ||
            normalizedName.endsWith(".png") || normalizedName.endsWith(".webp") -> PortToolType.IMAGE
        normalizedName.endsWith(".mp4") || normalizedName.endsWith(".mov") ||
            normalizedName.endsWith(".mkv") || normalizedName.endsWith(".avi") -> PortToolType.VIDEO
        else -> PortToolType.FILE
    }
}

private fun createCameraOutputUris(context: android.content.Context): Pair<Uri, Uri> {
    val cacheDir = File(context.cacheDir, "port_camera").apply { mkdirs() }
    val now = System.currentTimeMillis()
    val photoFile = File(cacheDir, "port_photo_$now.jpg")
    val videoFile = File(cacheDir, "port_video_$now.mp4")
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, photoFile) to
        FileProvider.getUriForFile(context, authority, videoFile)
}

private fun createCameraChooserIntent(
    context: android.content.Context,
    photoUri: Uri,
    videoUri: Uri
): Intent? {
    val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
    val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
        putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    val pm = context.packageManager
    val photoResolved = photoIntent.resolveActivity(pm)
    val videoResolved = videoIntent.resolveActivity(pm)
    if (photoResolved == null && videoResolved == null) return null

    return when {
        photoResolved != null && videoResolved != null -> {
            Intent.createChooser(photoIntent, context.getString(R.string.port_info_attachment_camera)).apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(videoIntent))
            }
        }
        photoResolved != null -> photoIntent
        else -> videoIntent
    }
}
