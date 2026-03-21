package com.gomgom.eod.feature.portinfo.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomgom.eod.R
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeEntry
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolDbState
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSearchMode
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolTempState
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolUiState
import com.gomgom.eod.feature.task.screens.TaskHamburgerMenuButton
import java.io.BufferedReader
import java.io.OutputStreamWriter
import android.media.MediaMetadataRetriever

@Composable
fun PortInfoTopScreen(
    uiState: PortToolUiState,
    dbState: PortToolDbState,
    tempState: PortToolTempState,
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit,
    onSaveClick: () -> Unit,
    onToggleVesselReporting: () -> Unit,
    onToggleAnchorage: () -> Unit,
    onToggleBerth: () -> Unit,
    onBundleChange: (PortRecordBundle) -> Unit,
    onCountrySuggestionClick: (PortUnlocodeEntry) -> Unit,
    onPortSuggestionClick: (PortUnlocodeEntry) -> Unit,
    onAttachmentsSelected: (List<PortAttachmentEntity>) -> Unit,
    onAttachmentDelete: (Long) -> Unit,
    onSourceSelect: (PortToolSource) -> Unit,
    onCountryKeywordChange: (String) -> Unit,
    onPortKeywordChange: (String) -> Unit,
    onSearchModeChange: () -> Unit,
    onExportJson: ((String) -> Unit) -> Unit,
    onExportCsv: ((String) -> Unit) -> Unit,
    onImportJson: (String, (Result<Unit>) -> Unit) -> Unit,
    onLiveSearchConfirm: () -> Unit,
    onLiveSearchCancel: () -> Unit,
    onLiveSearchHeaderDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDataManage by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var attachmentError by remember { mutableStateOf<String?>(null) }
    val canEdit = uiState.activeSource == PortToolSource.LOCAL
    val versionLabel = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    val hasSearchInput = uiState.portKeyword.isNotBlank()
    val displayResults = if (hasSearchInput) uiState.searchResults else emptyList()
    val activeBundle = tempState.editorBundle ?: dbState.selectedRecord

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

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val recordId = activeBundle?.record?.id ?: return@rememberLauncherForActivityResult
        val existing = activeBundle.attachments
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

    if (showDataManage) {
        AlertDialog(
            onDismissRequest = { showDataManage = false },
            title = {
                Text(
                    stringResource(R.string.port_info_data_manage),
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = {
                        onSourceSelect(PortToolSource.LOCAL)
                        showDataManage = false
                    }) { Text(stringResource(R.string.port_info_my_records)) }
                    TextButton(onClick = {
                        onSourceSelect(PortToolSource.SHARED)
                        showDataManage = false
                    }) { Text(stringResource(R.string.port_info_shared_records)) }
                    TextButton(onClick = { importJsonLauncher.launch(arrayOf("application/json")) }) {
                        Text(stringResource(R.string.port_info_import))
                    }
                    TextButton(onClick = { createJsonLauncher.launch("port_records.json") }) {
                        Text(stringResource(R.string.port_info_export_json))
                    }
                    TextButton(onClick = { createCsvLauncher.launch("port_records.csv") }) {
                        Text(stringResource(R.string.port_info_export_csv))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataManage = false }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }

    if (uiState.isLiveSearchPromptVisible) {
        AlertDialog(
            onDismissRequest = onLiveSearchCancel,
            title = {
                Text(
                    stringResource(R.string.port_info_live_search_title),
                    color = Color(0xFF123A73),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.port_info_live_search_message)) },
            confirmButton = {
                TextButton(onClick = onLiveSearchConfirm) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = onLiveSearchCancel) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text(stringResource(R.string.port_info_save_title), color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.port_info_save_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSaveConfirm = false
                    onSaveClick()
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (attachmentError != null) {
        AlertDialog(
            onDismissRequest = { attachmentError = null },
            title = { Text(stringResource(R.string.port_info_attachment_error_title), color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(attachmentError.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { attachmentError = null }) { Text(stringResource(R.string.common_ok)) }
            }
        )
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
                    IconButton(onClick = onBackClick) {
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
                    if (canEdit) {
                        FilledTonalIconButton(
                            onClick = onAddRecordClick,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.port_info_add_record))
                        }
                    }
                    if (canEdit) {
                        FilledTonalIconButton(
                            onClick = { showSaveConfirm = true },
                            enabled = tempState.hasPendingChanges,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = stringResource(R.string.port_info_save_record))
                        }
                    }
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

                Text(
                    text = stringResource(R.string.port_info_data_manage),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDataManage = true }
                        .padding(top = 2.dp),
                    color = Color(0xFF5D7598),
                    fontSize = 12.sp,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )

                if (uiState.isLiveSearchHeaderVisible && !uiState.isLiveSearchEnabled && uiState.pendingLiveSearchCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable(onClick = onLiveSearchConfirm),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCCEAF2FF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.port_info_live_search_pending, uiState.pendingLiveSearchCount),
                                color = Color(0xFF123A73),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.common_close),
                                color = Color(0xFF123A73),
                                modifier = Modifier.clickable(onClick = onLiveSearchHeaderDismiss)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.portKeyword,
                        onValueChange = onPortKeywordChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = {
                            Text(stringResource(R.string.port_info_search_placeholder))
                        }
                    )
                    Box {
                        FilledTonalIconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.port_info_filter))
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.port_info_filter_country_port)) },
                                onClick = {
                                    showFilterMenu = false
                                    if (uiState.searchMode != PortToolSearchMode.COUNTRY_PORT) onSearchModeChange()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.port_info_filter_full)) },
                                onClick = {
                                    showFilterMenu = false
                                    if (uiState.searchMode != PortToolSearchMode.FULL_TEXT) onSearchModeChange()
                                }
                            )
                        }
                    }
                }

                if (hasSearchInput && displayResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(displayResults.take(6), key = { it.id }) { record ->
                                SearchResultRow(
                                    record = record,
                                    countryQuery = uiState.portKeyword,
                                    detailQuery = uiState.portKeyword,
                                    onClick = { onRecordClick(record.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeBundle != null) {
                PortInfoRecordContent(
                    editable = canEdit,
                    bundle = activeBundle,
                    countrySuggestions = uiState.countrySuggestions,
                    portSuggestions = uiState.portSuggestions,
                    isVesselReportingExpanded = tempState.isVesselReportingExpanded,
                    isAnchorageExpanded = tempState.isAnchorageExpanded,
                    isBerthExpanded = tempState.isBerthExpanded,
                    onToggleVesselReporting = onToggleVesselReporting,
                    onToggleAnchorage = onToggleAnchorage,
                    onToggleBerth = onToggleBerth,
                    onBundleChange = onBundleChange,
                    onCountrySuggestionClick = onCountrySuggestionClick,
                    onPortSuggestionClick = onPortSuggestionClick,
                    onAddAttachmentClick = { attachmentLauncher.launch(arrayOf("image/*", "video/*", "application/pdf", "*/*")) },
                    onDeleteAttachmentClick = onAttachmentDelete
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    record: PortRecordEntity,
    countryQuery: String,
    detailQuery: String,
    onClick: () -> Unit
) {
    val country = record.countryCode.take(2).ifBlank { record.countryName.take(2) }
    val port = record.portName.let { if (it.length > 6) "${it.take(6)}..." else it }
    val preview = listOf(record.berthName, record.company, record.cargoName, record.generalRemark, record.caution)
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
        .take(24)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = highlightText(country, countryQuery),
            color = Color(0xFF123A73),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = highlightText(port, detailQuery),
            color = Color(0xFF123A73),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = highlightText(preview, detailQuery),
            color = Color(0xFF5D7598),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildAttachmentPayload(
    context: android.content.Context,
    recordId: Long,
    existing: List<PortAttachmentEntity>,
    uris: List<Uri>
): Result<List<PortAttachmentEntity>> = runCatching {
    val resolver = context.contentResolver
    val existingImageCount = existing.count { it.attachmentType == PortToolType.IMAGE }
    val existingVideoCount = existing.count { it.attachmentType == PortToolType.VIDEO }
    val existingFileCount = existing.count { it.attachmentType == PortToolType.FILE }
    val existingSize = existing.sumOf { it.fileSize }
    var imageCount = existingImageCount
    var videoCount = existingVideoCount
    var fileCount = existingFileCount
    var totalSize = existingSize
    val attachments = mutableListOf<PortAttachmentEntity>()
    val now = System.currentTimeMillis()

    uris.forEachIndexed { index, uri ->
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val mimeType = resolver.getType(uri).orEmpty()
        val fileSize = resolver.openAssetFileDescriptor(uri, "r")?.use { afd -> afd.length.coerceAtLeast(0L) } ?: 0L
        val attachmentType = when {
            mimeType.startsWith("image/") -> PortToolType.IMAGE
            mimeType.startsWith("video/") -> PortToolType.VIDEO
            else -> PortToolType.FILE
        }
        when (attachmentType) {
            PortToolType.IMAGE -> {
                imageCount++
                if (imageCount > 20) error(context.getString(R.string.port_info_attachment_limit_image))
            }
            PortToolType.VIDEO -> {
                videoCount++
                if (videoCount > 1) error(context.getString(R.string.port_info_attachment_limit_video_count))
                val durationMs = MediaMetadataRetriever().run {
                    setDataSource(context, uri)
                    val duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    release()
                    duration
                }
                if (durationMs > 60_000L) error(context.getString(R.string.port_info_attachment_limit_video_duration))
            }
            else -> {
                fileCount++
                if (fileCount > 10) error(context.getString(R.string.port_info_attachment_limit_file))
            }
        }
        totalSize += fileSize
        if (totalSize > 10L * 1024 * 1024) error(context.getString(R.string.port_info_attachment_limit_total))

        val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }.orEmpty()
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

@Composable
private fun highlightText(text: String, query: String) = buildAnnotatedString {
    if (query.isBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    val normalized = text.lowercase()
    val needle = query.lowercase().trim()
    val start = normalized.indexOf(needle)
    if (start < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, start))
    pushStyle(SpanStyle(color = Color(0xFFE0A100), fontWeight = FontWeight.Bold))
    append(text.substring(start, start + needle.length.coerceAtMost(text.length - start)))
    pop()
    append(text.substring((start + needle.length).coerceAtMost(text.length)))
}
