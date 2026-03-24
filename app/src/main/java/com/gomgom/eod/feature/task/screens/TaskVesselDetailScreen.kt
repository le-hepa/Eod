package com.gomgom.eod.feature.task.screens

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Videocam
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.R
import com.gomgom.eod.core.navigation.TaskAlarmNavigationBridge
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkItem
import com.gomgom.eod.feature.task.viewmodel.TaskPresetWorkViewModel
import com.gomgom.eod.feature.task.alarm.TaskAlarmScheduler
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordAttachmentType
import com.gomgom.eod.feature.task.model.TaskWorkRecordItem
import com.gomgom.eod.feature.task.model.TaskWorkRecordStatus
import com.gomgom.eod.feature.task.model.TaskWorkRecordType
import com.gomgom.eod.feature.task.viewmodel.TaskAlarmSettingsViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskWorkRecordViewModel
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val RecordBackground = Color(0xFFF5F8FC)
private val RecordCardColor = Color.White
private val RecordAccentSurface = Color(0xFFEAF2FF)
private val RecordPrimaryText = Color(0xFF123A73)
private val RecordSecondaryText = Color(0xFF6E85A3)
private val RecordDivider = Color(0xFFDCE5F0)

private val CalendarBadgeBlue = Color(0xFF4A78D1)
private val CalendarBadgeOrange = Color(0xFFF3B16D)
private val CalendarBadgeRed = Color(0xFFE05858)

private val WorkBadgeBaseGray = Color(0xFFD8D8D8)
private val WorkBadgeOrange = Color(0xFFF3B16D)
private val WorkBadgeGreen = Color(0xFF59B36A)
private val WorkBadgeRed = Color(0xFFE05858)
private val WorkBadgeBlue = Color(0xFF4A78D1)
private const val WORK_RECORD_LIST_PAGE_SIZE = 8
private const val WORK_RECORD_LIST_INITIAL_PAGE_GROUPS = 5
private const val MAX_ATTACHMENT_COUNT = 20
private const val MAX_VIDEO_DURATION_MS = 5 * 60 * 1000L

@Composable
fun TaskVesselDetailScreen(
    vesselId: Long,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val context = LocalContext.current
    val topViewModel: TaskTopViewModel = viewModel()
    val workViewModel: TaskPresetWorkViewModel = viewModel()
    val workRecordViewModel: TaskWorkRecordViewModel = viewModel()
    val alarmSettingsViewModel: TaskAlarmSettingsViewModel = viewModel()
    val uiState by topViewModel.uiState.collectAsState()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()
    val alarmNavigationTarget by TaskAlarmNavigationBridge.target.collectAsState()
    val vesselItem = uiState.vesselItems.firstOrNull { it.id == vesselId }
    val activePreset = presetGroups.firstOrNull { it.enabled }
    val presetWorks by workViewModel.worksForPreset(activePreset?.id ?: 0L)
        .collectAsState(initial = emptyList())
    val vesselRecords by workRecordViewModel.recordsForVessel(vesselId)
        .collectAsState(initial = emptyList())

    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var lastCalendarTappedDate by remember { mutableStateOf<LocalDate?>(null) }
    var monthPickerVisible by remember { mutableStateOf(false) }
    var recordDetailVisible by remember { mutableStateOf(false) }
    var recordListVisible by remember { mutableStateOf(false) }
    var recordListDate by remember { mutableStateOf(LocalDate.now()) }
    var recordListFilter by remember { mutableStateOf(WorkRecordListFilter.TODAY) }
    var recordListQuery by remember { mutableStateOf("") }
    var recordListLoadedPageGroups by remember { mutableStateOf(1) }
    var nextPageConfirmVisible by remember { mutableStateOf(false) }
    var recordStatusPickerRecord by remember { mutableStateOf<TaskWorkRecordItem?>(null) }
    var recentRecordVisible by remember { mutableStateOf(false) }
    var typePickerVisible by remember { mutableStateOf(false) }
    var presetPickerVisible by remember { mutableStateOf(false) }
    var selectedPresetWorkId by remember { mutableStateOf<Long?>(null) }
    var currentRecordId by remember { mutableStateOf<Long?>(null) }
    var workInputMode by remember { mutableStateOf(WorkInputMode.NONE) }
    var workName by remember { mutableStateOf("") }
    var referenceText by remember { mutableStateOf("") }
    var cycleNumberText by remember { mutableStateOf("") }
    var cycleUnitText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }
    var attachmentItems by remember { mutableStateOf<List<TaskWorkRecordAttachmentItem>>(emptyList()) }
    var nextEditorAttachmentId by remember { mutableStateOf(-1L) }
    var selectedStatus by remember { mutableStateOf(defaultStatusOptions().first()) }
    var initialEditorSnapshot by remember { mutableStateOf<RecordEditorSnapshot?>(null) }
    var irregularDuplicateWarningVisible by remember { mutableStateOf(false) }
    var lastIrregularWarningName by remember { mutableStateOf("") }
    var attachmentPickerVisible by remember { mutableStateOf(false) }
    var imagePreviewAttachment by remember { mutableStateOf<TaskWorkRecordAttachmentItem?>(null) }
    var attachmentActionTarget by remember { mutableStateOf<TaskWorkRecordAttachmentItem?>(null) }
    var attachmentDeleteTarget by remember { mutableStateOf<TaskWorkRecordAttachmentItem?>(null) }
    var pendingPhotoCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPhotoCaptureName by remember { mutableStateOf("") }
    var pendingVideoCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVideoCaptureName by remember { mutableStateOf("") }
    var attachmentLimitWarningVisible by remember { mutableStateOf(false) }
    var attachmentVideoLimitWarningVisible by remember { mutableStateOf(false) }
    var pdfGenerating by remember { mutableStateOf(false) }
    var deleteRecordConfirmVisible by remember { mutableStateOf(false) }
    var pdfSaveConfirmVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val currentEditorSnapshot by remember(
        workInputMode,
        workName,
        referenceText,
        cycleNumberText,
        cycleUnitText,
        selectedStatus.workBadgeType,
        commentText,
        attachmentItems
    ) {
        derivedStateOf {
            RecordEditorSnapshot(
                workInputMode = workInputMode,
                workName = workName,
                referenceText = referenceText,
                cycleNumberText = cycleNumberText,
                cycleUnitText = cycleUnitText,
                selectedStatus = selectedStatus.workBadgeType,
                commentText = commentText,
                attachmentKeys = attachmentItems.map { "${it.type}:${it.uri}" }
            )
        }
    }
    val isSaveEnabled = recordDetailVisible && initialEditorSnapshot != null && currentEditorSnapshot != initialEditorSnapshot

    val selectedDateRecords by workRecordViewModel.recordsForDate(vesselId, selectedDate)
        .collectAsState(initial = emptyList())
    val recordsByDate by workRecordViewModel.recordsGroupedByDate(vesselId)
        .collectAsState(initial = emptyMap())
    val selectedDateDisplayRecords = selectedDateRecords
    val selectedDateRegularRecords by workRecordViewModel
        .recordsForDateByType(vesselId, selectedDate, TaskWorkRecordType.REGULAR)
        .collectAsState(initial = emptyList())
    val selectedDateIrregularRecords by workRecordViewModel
        .recordsForDateByType(vesselId, selectedDate, TaskWorkRecordType.IRREGULAR)
        .collectAsState(initial = emptyList())
    val recentRecordItems by workRecordViewModel.recentRecordsFor(
        vesselId = vesselId,
        recordType = when (workInputMode) {
            WorkInputMode.REGULAR -> TaskWorkRecordType.REGULAR
            WorkInputMode.IRREGULAR -> TaskWorkRecordType.IRREGULAR
            WorkInputMode.NONE -> TaskWorkRecordType.IRREGULAR
        },
        presetWorkId = if (workInputMode == WorkInputMode.REGULAR) selectedPresetWorkId else null,
        workName = workName
    ).collectAsState(initial = emptyList())
    val regularSummaryText = buildStatusSummaryText(selectedDateRegularRecords)
    val irregularSummaryText = buildStatusSummaryText(selectedDateIrregularRecords)
    val recordListFilteredItems by workRecordViewModel.listRecordsForVessel(
        vesselId = vesselId,
        recordDate = recordListDate,
        includeAllDates = recordListFilter == WorkRecordListFilter.ALL,
        query = recordListQuery
    ).collectAsState(initial = emptyList())
    val recordListVisibleCount =
        WORK_RECORD_LIST_PAGE_SIZE * WORK_RECORD_LIST_INITIAL_PAGE_GROUPS * recordListLoadedPageGroups
    val recordListItems by remember(recordListFilteredItems, recordListFilter, recordListVisibleCount) {
        derivedStateOf {
            if (recordListFilter == WorkRecordListFilter.TODAY) recordListFilteredItems
            else recordListFilteredItems.take(recordListVisibleCount)
        }
    }
    val hasMoreRecordPages by remember(recordListFilter, recordListFilteredItems, recordListItems) {
        derivedStateOf {
            recordListFilter == WorkRecordListFilter.ALL &&
                recordListFilteredItems.size > recordListItems.size
        }
    }

    val addAttachments: (TaskWorkRecordAttachmentType, List<Pair<Uri, String>>) -> Unit = { type, items ->
        val resolver = context.contentResolver
        val available = (MAX_ATTACHMENT_COUNT - attachmentItems.size).coerceAtLeast(0)
        if (items.size > available) {
            attachmentLimitWarningVisible = true
        }
        val newItems = items.take(available).mapNotNull { (uri, displayName) ->
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }
            if (type == TaskWorkRecordAttachmentType.VIDEO) {
                val duration = queryVideoDurationMs(context, uri)
                if (duration == null || duration > MAX_VIDEO_DURATION_MS) {
                    attachmentVideoLimitWarningVisible = true
                    return@mapNotNull null
                }
            }
            val generatedId = nextEditorAttachmentId
            nextEditorAttachmentId -= 1L
            uri.takeIf { it.toString().isNotBlank() }?.let {
                TaskWorkRecordAttachmentItem(
                    id = generatedId,
                    recordId = currentRecordId ?: 0L,
                    type = type,
                    uri = it.toString(),
                    displayName = displayName.ifBlank { queryAttachmentDisplayName(context, it) }
                )
            }
        }
        attachmentItems = attachmentItems + newItems
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            addAttachments(TaskWorkRecordAttachmentType.IMAGE, uris.map { it to queryAttachmentDisplayName(context, it) })
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            addAttachments(TaskWorkRecordAttachmentType.VIDEO, uris.map { it to queryAttachmentDisplayName(context, it) })
        }
    }

    val photoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoCaptureUri
        if (success && uri != null) {
            addAttachments(TaskWorkRecordAttachmentType.IMAGE, listOf(uri to pendingPhotoCaptureName))
        }
        pendingPhotoCaptureUri = null
        pendingPhotoCaptureName = ""
    }

    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = pendingVideoCaptureUri
        if (result.resultCode == android.app.Activity.RESULT_OK && uri != null) {
            addAttachments(TaskWorkRecordAttachmentType.VIDEO, listOf(uri to pendingVideoCaptureName))
        }
        pendingVideoCaptureUri = null
        pendingVideoCaptureName = ""
    }

    LaunchedEffect(recordListFilter, recordListQuery) {
        recordListLoadedPageGroups = 1
        nextPageConfirmVisible = false
    }

    LaunchedEffect(alarmNavigationTarget, vesselRecords) {
        val target = alarmNavigationTarget ?: return@LaunchedEffect
        if (target.vesselId != vesselId) return@LaunchedEffect
        selectedDate = target.targetDate
        visibleMonth = YearMonth.from(target.targetDate)
        recordDetailVisible = false
        recordListVisible = false
        lastCalendarTappedDate = null
        TaskAlarmNavigationBridge.clear()
    }

    val openNewRecordEditor: (LocalDate) -> Unit = { targetDate ->
        selectedDate = targetDate
        currentRecordId = null
        selectedPresetWorkId = null
        workInputMode = WorkInputMode.NONE
        workName = ""
        referenceText = ""
        cycleNumberText = ""
        cycleUnitText = ""
        commentText = ""
        attachmentItems = emptyList()
        selectedStatus = defaultStatusOptions().first()
        val emptySnapshot = RecordEditorSnapshot(
            workInputMode = WorkInputMode.NONE,
            workName = "",
            referenceText = "",
            cycleNumberText = "",
            cycleUnitText = "",
            selectedStatus = defaultStatusOptions().first().workBadgeType,
            commentText = "",
            attachmentKeys = emptyList()
        )
        initialEditorSnapshot = emptySnapshot
        recordDetailVisible = true
    }

    BackHandler(enabled = recordDetailVisible || recordListVisible) {
        when {
            recordDetailVisible -> recordDetailVisible = false
            recordListVisible -> recordListVisible = false
        }
    }

    val loadRecordIntoEditor: (TaskWorkRecordItem) -> Unit = { loadedRecord ->
        currentRecordId = loadedRecord.id
        selectedDate = loadedRecord.recordDate
        workInputMode = when (loadedRecord.recordType) {
            TaskWorkRecordType.REGULAR -> WorkInputMode.REGULAR
            TaskWorkRecordType.IRREGULAR -> WorkInputMode.IRREGULAR
        }
        selectedPresetWorkId = loadedRecord.presetWorkId
        workName = loadedRecord.workName
        referenceText = loadedRecord.reference
        cycleNumberText = loadedRecord.cycleNumberText
        cycleUnitText = loadedRecord.cycleUnitText
        selectedStatus = loadedRecord.status.toRecordStatusOption()
        commentText = loadedRecord.comment
        attachmentItems = loadedRecord.attachments
        initialEditorSnapshot = RecordEditorSnapshot(
            workInputMode = workInputMode,
            workName = loadedRecord.workName,
            referenceText = loadedRecord.reference,
            cycleNumberText = loadedRecord.cycleNumberText,
            cycleUnitText = loadedRecord.cycleUnitText,
            selectedStatus = loadedRecord.status.toWorkBadgeType(),
            commentText = loadedRecord.comment,
            attachmentKeys = loadedRecord.attachments.map { "${it.type}:${it.uri}" }
        )
        recordDetailVisible = true
    }

    if (monthPickerVisible) {
        MonthPickerDialog(
            initialMonth = visibleMonth,
            onDismiss = { monthPickerVisible = false },
            onApply = { selectedMonth ->
                visibleMonth = selectedMonth
                selectedDate = selectedDate.withYear(selectedMonth.year)
                    .withMonth(selectedMonth.monthValue)
                    .withDayOfMonth(minOf(selectedDate.dayOfMonth, selectedMonth.lengthOfMonth()))
                monthPickerVisible = false
            }
        )
    }

    if (typePickerVisible) {
        WorkTypePickerDialog(
            onDismiss = { typePickerVisible = false },
            onRegularClick = {
                typePickerVisible = false
                presetPickerVisible = true
            },
            onIrregularClick = {
                typePickerVisible = false
                workInputMode = WorkInputMode.IRREGULAR
                selectedPresetWorkId = null
                workName = ""
                referenceText = ""
                cycleNumberText = ""
                cycleUnitText = ""
                selectedStatus = statusOptionsFor(WorkInputMode.IRREGULAR).first()
            }
        )
    }

    if (presetPickerVisible) {
        PresetWorkPickerDialog(
            presetName = activePreset?.name.orEmpty(),
            works = presetWorks,
            selectedWorkId = selectedPresetWorkId,
            onDismiss = { presetPickerVisible = false },
            onSelect = { selectedPresetWorkId = it },
            onApply = { selectedWork ->
                workInputMode = WorkInputMode.REGULAR
                selectedPresetWorkId = selectedWork.id
                workName = selectedWork.name
                referenceText = selectedWork.reference
                cycleNumberText = selectedWork.cycleNumber.toString()
                cycleUnitText = cycleUnitShortLabel(selectedWork)
                selectedStatus = statusOptionsFor(WorkInputMode.REGULAR).first()
                presetPickerVisible = false
            }
        )
    }

    if (attachmentPickerVisible) {
        AttachmentTypePickerDialog(
            onDismiss = { attachmentPickerVisible = false },
            onImageCaptureClick = {
                attachmentPickerVisible = false
                val file = createAttachmentCaptureFile(context, "IMG", "jpg")
                pendingPhotoCaptureUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                pendingPhotoCaptureName = file.name
                pendingPhotoCaptureUri?.let(photoCaptureLauncher::launch)
            },
            onImageClick = {
                attachmentPickerVisible = false
                imagePickerLauncher.launch(arrayOf("image/*"))
            },
            onVideoCaptureClick = {
                attachmentPickerVisible = false
                val file = createAttachmentCaptureFile(context, "VID", "mp4")
                pendingVideoCaptureUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                pendingVideoCaptureName = file.name
                pendingVideoCaptureUri?.let { captureUri ->
                    videoCaptureLauncher.launch(
                        Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE).apply {
                            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, captureUri)
                            putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, 300)
                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    )
                }
            },
            onVideoClick = {
                attachmentPickerVisible = false
                videoPickerLauncher.launch(arrayOf("video/*"))
            }
        )
    }

    if (recentRecordVisible) {
        RecentRecordDialog(
            workName = workName,
            recentRecordItems = recentRecordItems,
            onDismiss = { recentRecordVisible = false },
            onRecordClick = { recordId ->
                val loadedRecord = workRecordViewModel.getRecord(recordId) ?: return@RecentRecordDialog
                recentRecordVisible = false
                loadRecordIntoEditor(loadedRecord)
            }
        )
    }

    if (recordStatusPickerRecord != null) {
        RecordStatusPickerDialog(
            record = recordStatusPickerRecord!!,
            onDismiss = { recordStatusPickerRecord = null },
            onSelect = { selectedOption ->
                val targetRecord = recordStatusPickerRecord ?: return@RecordStatusPickerDialog
                val updatedStatus = selectedOption.workBadgeType.toTaskWorkRecordStatus()
                if (workRecordViewModel.updateRecordStatus(targetRecord.id, updatedStatus)) {
                    if (
                        targetRecord.recordType == TaskWorkRecordType.IRREGULAR &&
                        updatedStatus == TaskWorkRecordStatus.NonRegularDone
                    ) {
                        alarmSettingsViewModel.setIrregularWorkAlarmEnabled(
                            vesselId = targetRecord.vesselId,
                            workName = targetRecord.workName,
                            enabled = false
                        )
                    }
                    if (currentRecordId == targetRecord.id) {
                        selectedStatus = selectedOption
                        initialEditorSnapshot = initialEditorSnapshot?.copy(selectedStatus = selectedOption.workBadgeType)
                    }
                }
                recordStatusPickerRecord = null
            }
        )
    }

    if (irregularDuplicateWarningVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { irregularDuplicateWarningVisible = false },
            confirmButton = {
                TextButton(onClick = { irregularDuplicateWarningVisible = false }) {
                    Text(text = stringResource(R.string.common_ok))
                }
            },
            title = { Text(stringResource(R.string.work_detail_irregular_duplicate_title)) },
            text = { Text(stringResource(R.string.work_detail_irregular_duplicate_body)) },
            containerColor = Color.White
        )
    }

    if (nextPageConfirmVisible) {
        NextPageConfirmDialog(
            onDismiss = { nextPageConfirmVisible = false },
            onConfirm = {
                nextPageConfirmVisible = false
                recordListLoadedPageGroups += 1
            }
        )
    }

    if (appInfoVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { appInfoVisible = false },
            confirmButton = {
                TextButton(onClick = { appInfoVisible = false }) {
                    Text(text = stringResource(R.string.home_app_info_confirm))
                }
            },
            title = { Text(text = stringResource(R.string.home_app_info_title)) },
            text = {
                Column {
                    Text(
                        text = "${stringResource(R.string.home_app_info_name_label)}: ${stringResource(R.string.home_app_info_name_value)}",
                        color = RecordPrimaryText
                    )
                    Text(
                        text = "${stringResource(R.string.home_app_info_version_label)}: ${stringResource(R.string.home_app_info_version_value)}",
                        color = RecordSecondaryText
                    )
                }
            },
            containerColor = RecordCardColor
        )
    }

    attachmentActionTarget?.let { target ->
        AttachmentActionDialog(
            attachment = target,
            onDismiss = { attachmentActionTarget = null },
            onDeleteClick = {
                attachmentActionTarget = null
                attachmentDeleteTarget = target
            }
        )
    }

    attachmentDeleteTarget?.let { target ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { attachmentDeleteTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        attachmentItems = attachmentItems.filterNot { it.id == target.id }
                        attachmentDeleteTarget = null
                    }
                ) {
                    Text(text = stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { attachmentDeleteTarget = null }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.work_detail_attachment_delete_title)) },
            text = { Text(text = stringResource(R.string.work_detail_attachment_delete_message)) },
            containerColor = Color.White
        )
    }

    imagePreviewAttachment?.let { target ->
        ImagePreviewDialog(
            attachment = target,
            onDismiss = { imagePreviewAttachment = null }
        )
    }

    if (attachmentLimitWarningVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { attachmentLimitWarningVisible = false },
            confirmButton = {
                TextButton(onClick = { attachmentLimitWarningVisible = false }) {
                    Text(text = stringResource(R.string.common_ok))
                }
            },
            title = { Text(text = stringResource(R.string.work_detail_attachment_limit_title)) },
            text = { Text(text = stringResource(R.string.work_detail_attachment_limit_message)) },
            containerColor = Color.White
        )
    }

    if (attachmentVideoLimitWarningVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { attachmentVideoLimitWarningVisible = false },
            confirmButton = {
                TextButton(onClick = { attachmentVideoLimitWarningVisible = false }) {
                    Text(text = stringResource(R.string.common_ok))
                }
            },
            title = { Text(text = stringResource(R.string.work_detail_attachment_video_limit_title)) },
            text = { Text(text = stringResource(R.string.work_detail_attachment_video_limit_message)) },
            containerColor = Color.White
        )
    }

    if (deleteRecordConfirmVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteRecordConfirmVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteRecordConfirmVisible = false
                        val recordId = currentRecordId ?: return@TextButton
                        val deleted = workRecordViewModel.deleteRecord(recordId)
                        if (deleted) {
                            currentRecordId = null
                            selectedPresetWorkId = null
                            workInputMode = WorkInputMode.NONE
                            workName = ""
                            referenceText = ""
                            cycleNumberText = ""
                            cycleUnitText = ""
                            commentText = ""
                            attachmentItems = emptyList()
                            selectedStatus = defaultStatusOptions().first()
                            initialEditorSnapshot = null
                            recordDetailVisible = false
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.work_detail_pdf_delete_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.common_delete), color = Color(0xFF8E1D1D))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecordConfirmVisible = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.work_detail_delete_confirm_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.work_detail_delete_confirm_body),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            containerColor = Color.White
        )
    }

    if (pdfSaveConfirmVisible) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pdfSaveConfirmVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pdfSaveConfirmVisible = false
                        pdfGenerating = true
                        val pdfVesselName = vesselItem?.name.orEmpty()
                        val pdfPresetName = activePreset?.name.orEmpty()
                        val pdfRecordDate = selectedDate
                        val pdfWorkName = workName
                        val pdfCycleNumberText = cycleNumberText
                        val pdfCycleUnitText = cycleUnitText
                        val pdfStatusLabel = context.getString(selectedStatus.labelResId)
                        val pdfComment = commentText
                        val pdfAttachments = attachmentItems
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                generateWorkRecordPdf(
                                    context = context,
                                    vesselName = pdfVesselName,
                                    presetName = pdfPresetName,
                                    recordDate = pdfRecordDate,
                                    workName = pdfWorkName,
                                    cycleNumberText = pdfCycleNumberText,
                                    cycleUnitText = pdfCycleUnitText,
                                    statusLabel = pdfStatusLabel,
                                    comment = pdfComment,
                                    attachments = pdfAttachments
                                )
                            }
                            pdfGenerating = false
                            result.onSuccess { uri ->
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.work_detail_pdf_saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                                runCatching {
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            },
                                            null
                                        )
                                    )
                                }
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.work_detail_pdf_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pdfSaveConfirmVisible = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.work_detail_pdf_save),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.work_detail_pdf_confirm_body),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            containerColor = Color.White
        )
    }

    if (pdfGenerating) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = null,
            text = {
                Text(
                    text = stringResource(R.string.work_detail_pdf_generating),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordPrimaryText,
                    fontWeight = FontWeight.SemiBold
                )
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = RecordBackground,
        topBar = {
            TopBar(
                title = if (recordDetailVisible) {
                    selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault()))
                } else if (recordListVisible) {
                    recordListDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault()))
                } else {
                    vesselItem?.name.orEmpty()
                },
                menuExpanded = menuExpanded,
                onBackClick = {
                    when {
                        recordDetailVisible -> recordDetailVisible = false
                        recordListVisible -> recordListVisible = false
                        else -> onBackClick()
                    }
                },
                onMenuClick = { menuExpanded = true },
                onDismissMenu = { menuExpanded = false },
                onHomeClick = onHomeClick,
                onKorClick = onKorClick,
                onEngClick = onEngClick,
                onAppInfoClick = { appInfoVisible = true },
                onGuideClick = onGuideClick,
                onContactClick = onContactClick,
                onExitClick = onExitClick
            )
        }
    ) { innerPadding ->
        if (recordDetailVisible) {
            WorkRecordDetailContent(
                innerPadding = innerPadding,
                selectedDate = selectedDate,
                listItems = selectedDateDisplayRecords,
                recentRecordItems = recentRecordItems,
                workName = workName,
                workInputMode = workInputMode,
                referenceText = referenceText,
                cycleNumberText = cycleNumberText,
                cycleUnitText = cycleUnitText,
                commentText = commentText,
                selectedStatus = selectedStatus,
                attachmentItems = attachmentItems,
                isSaveEnabled = isSaveEnabled,
                onWorkFieldClick = { typePickerVisible = true },
                isWorkFieldSelectionEnabled = !(currentRecordId != null && workInputMode == WorkInputMode.REGULAR),
                onWorkNameChange = {
                    workName = it
                    lastIrregularWarningName = ""
                },
                onWorkNameFocusChanged = { isFocused ->
                    if (!isFocused && workInputMode == WorkInputMode.IRREGULAR) {
                        val trimmedName = workName.trim()
                        val duplicatedRegularName = trimmedName.isNotBlank() && presetWorks.any { it.name.trim() == trimmedName }
                        if (duplicatedRegularName && lastIrregularWarningName != trimmedName) {
                            lastIrregularWarningName = trimmedName
                            irregularDuplicateWarningVisible = true
                        }
                    }
                },
                onReferenceChange = { referenceText = it },
                onCycleNumberChange = { cycleNumberText = it },
                onCycleUnitChange = { cycleUnitText = it },
                onCommentChange = { commentText = it },
                onStatusChange = { selectedStatus = it },
                onAttachmentAddClick = { attachmentPickerVisible = true },
                onAttachmentPreviewClick = { attachment ->
                    when (attachment.type) {
                        TaskWorkRecordAttachmentType.IMAGE -> imagePreviewAttachment = attachment
                        TaskWorkRecordAttachmentType.VIDEO -> runCatching {
                            val uri = Uri.parse(attachment.uri)
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "video/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            )
                        }
                    }
                },
                onAttachmentDeleteClick = { attachment ->
                    attachmentActionTarget = attachment
                },
                onSaveClick = {
                    val recordType = when (workInputMode) {
                        WorkInputMode.REGULAR -> TaskWorkRecordType.REGULAR
                        WorkInputMode.IRREGULAR -> TaskWorkRecordType.IRREGULAR
                        WorkInputMode.NONE -> null
                    }
                    if (recordType != null) {
                        val savedRecordId = workRecordViewModel.saveRecord(
                            recordId = currentRecordId,
                            vesselId = vesselId,
                            recordDate = selectedDate,
                            presetWorkId = if (workInputMode == WorkInputMode.REGULAR) selectedPresetWorkId else null,
                            recordType = recordType,
                            workName = workName,
                            reference = referenceText,
                            cycleNumberText = cycleNumberText,
                            cycleUnitText = cycleUnitText,
                            status = selectedStatus.workBadgeType.toTaskWorkRecordStatus(),
                            comment = commentText,
                            attachments = attachmentItems
                        )

                        if (savedRecordId != null) {
                            currentRecordId = savedRecordId
                            attachmentItems = attachmentItems.map { it.copy(recordId = savedRecordId) }
                            if (
                                recordType == TaskWorkRecordType.IRREGULAR &&
                                selectedStatus.workBadgeType.toTaskWorkRecordStatus() == TaskWorkRecordStatus.NonRegularDone
                            ) {
                                alarmSettingsViewModel.setIrregularWorkAlarmEnabled(
                                    vesselId = vesselId,
                                    workName = workName,
                                    enabled = false
                                )
                            }
                            initialEditorSnapshot = currentEditorSnapshot.copy(
                                attachmentKeys = attachmentItems.map { "${it.type}:${it.uri}" }
                            )
                        }
                    }
                },
                onEditClick = {
                    openNewRecordEditor(selectedDate)
                },
                onRecentRecordClick = {
                    recentRecordVisible = true
                },
                onPdfClick = {
                    pdfSaveConfirmVisible = true
                },
                onDeleteClick = {
                    if (currentRecordId != null) {
                        deleteRecordConfirmVisible = true
                    }
                },
                isDeleteEnabled = currentRecordId != null,
                onRecordClick = { record ->
                    loadRecordIntoEditor(record)
                },
                onRecordLongClick = { record ->
                    recordStatusPickerRecord = record
                }
            )
        } else {
            if (recordListVisible) {
                WorkRecordListContent(
                    innerPadding = innerPadding,
                    selectedDate = recordListDate,
                    filter = recordListFilter,
                    query = recordListQuery,
                    items = recordListItems,
                    hasMorePages = hasMoreRecordPages,
                    onBackClick = { recordListVisible = false },
                    onFilterChange = { recordListFilter = it },
                    onQueryChange = { recordListQuery = it },
                    onNextPageClick = { nextPageConfirmVisible = true },
                    onNewRecordClick = { openNewRecordEditor(recordListDate) },
                    onRecordClick = { record ->
                        loadRecordIntoEditor(record)
                    },
                    onRecordLongClick = { record ->
                        recordStatusPickerRecord = record
                    }
                )
            } else {
                WorkRecordHomeContent(
                    innerPadding = innerPadding,
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    listItems = selectedDateDisplayRecords,
                    recordsByDate = recordsByDate,
                    regularSummaryText = regularSummaryText,
                    irregularSummaryText = irregularSummaryText,
                    onMonthTitleClick = { monthPickerVisible = true },
                    onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                    onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
                    onSelectDate = { clickedDate ->
                        if (clickedDate == selectedDate && lastCalendarTappedDate == clickedDate) {
                            recordListDate = clickedDate
                            recordListFilter = WorkRecordListFilter.TODAY
                            recordListQuery = ""
                            recordListVisible = true
                            lastCalendarTappedDate = null
                        } else {
                            selectedDate = clickedDate
                            lastCalendarTappedDate = clickedDate
                        }
                    },
                    onEditClick = { openNewRecordEditor(selectedDate) },
                    onRecordClick = { record ->
                        loadRecordIntoEditor(record)
                    },
                    onRecordLongClick = { record ->
                        recordStatusPickerRecord = record
                    }
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    menuExpanded: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            TextButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_close),
                    tint = RecordPrimaryText,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = RecordPrimaryText,
                textAlign = TextAlign.Center
            )
        }
        TaskHamburgerMenuButton(
            expanded = menuExpanded,
            onExpandedChange = { expanded ->
                if (expanded) onMenuClick() else onDismissMenu()
            },
            iconTint = RecordPrimaryText,
            menuBackgroundColor = RecordCardColor,
            dividerColor = RecordDivider,
            textColor = RecordPrimaryText,
            onHomeClick = onHomeClick,
            onKorClick = onKorClick,
            onEngClick = onEngClick,
            onAppInfoClick = onAppInfoClick,
            onGuideClick = onGuideClick,
            onContactClick = onContactClick,
            onExitClick = onExitClick
        )
    }
}

@Composable
private fun WorkRecordHomeContent(
    innerPadding: PaddingValues,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    listItems: List<TaskWorkRecordItem>,
    recordsByDate: Map<LocalDate, List<TaskWorkRecordItem>>,
    regularSummaryText: String,
    irregularSummaryText: String,
    onMonthTitleClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    onRecordClick: (TaskWorkRecordItem) -> Unit,
    onRecordLongClick: (TaskWorkRecordItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RecordBackground)
            .padding(innerPadding)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CalendarCard(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                calendarBadgeForDate = { date ->
                    buildCalendarBadgeType(
                        date = date,
                        records = recordsByDate[date].orEmpty()
                    )
                },
                onMonthTitleClick = onMonthTitleClick,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectDate = onSelectDate
            )
        }
        item {
            SummaryCard(
                selectedDate = selectedDate,
                regularSummaryText = regularSummaryText,
                irregularSummaryText = irregularSummaryText,
                onEditClick = onEditClick
            )
        }
        item { WorkListHeader() }
        if (listItems.isEmpty()) {
            item { EmptyWorkListCard() }
        } else {
            items(listItems, key = { it.id }) { record ->
                WorkRecordListRow(
                    record = record,
                    onClick = { onRecordClick(record) },
                    onLongClick = { onRecordLongClick(record) }
                )
            }
        }
    }
}

@Composable
private fun WorkRecordListContent(
    innerPadding: PaddingValues,
    selectedDate: LocalDate,
    filter: WorkRecordListFilter,
    query: String,
    items: List<TaskWorkRecordItem>,
    hasMorePages: Boolean,
    onBackClick: () -> Unit,
    onFilterChange: (WorkRecordListFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onNextPageClick: () -> Unit,
    onNewRecordClick: () -> Unit,
    onRecordClick: (TaskWorkRecordItem) -> Unit,
    onRecordLongClick: (TaskWorkRecordItem) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RecordBackground)
            .padding(innerPadding)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.clickable { filterExpanded = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = RecordCardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = RecordPrimaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = when (filter) {
                                WorkRecordListFilter.TODAY -> stringResource(R.string.work_record_list_filter_today)
                                WorkRecordListFilter.ALL -> stringResource(R.string.work_record_list_filter_all)
                            },
                            color = RecordPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        modifier = Modifier.background(RecordCardColor)
                    ) {
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.work_record_list_filter_today)) },
                            onClick = {
                                filterExpanded = false
                                onFilterChange(WorkRecordListFilter.TODAY)
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.work_record_list_filter_all)) },
                            onClick = {
                                filterExpanded = false
                                onFilterChange(WorkRecordListFilter.ALL)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.work_record_home_search_hint),
                            color = RecordSecondaryText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = RecordPrimaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )

                TextButton(
                    onClick = onNewRecordClick,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.work_record_home_edit),
                        tint = RecordPrimaryText,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (filter == WorkRecordListFilter.TODAY) {
            item {
                TodayWorkListHeader(selectedDate = selectedDate)
            }
        }

        if (items.isEmpty()) {
            item { EmptyWorkListCard() }
        } else {
            items(items, key = { it.id }) { record ->
                WorkRecordListRow(
                    record = record,
                    onClick = { onRecordClick(record) },
                    onLongClick = { onRecordLongClick(record) }
                )
            }
        }

        if (hasMorePages) {
            item {
                NextPageButton(onClick = onNextPageClick)
            }
        }
    }
}

@Composable
private fun NextPageButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = stringResource(R.string.work_record_list_next_page),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = RecordPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MenuText(text: String) {
    Text(
        text = text,
        color = RecordPrimaryText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun NextPageConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.work_record_list_next_page),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordPrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.work_record_list_next_page_message),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordSecondaryText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.common_cancel), color = RecordSecondaryText)
                    }
                    TextButton(onClick = onConfirm) {
                        Text(text = stringResource(R.string.common_ok), color = RecordPrimaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    calendarBadgeForDate: (LocalDate) -> CalendarBadgeType,
    onMonthTitleClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy.MM", Locale.getDefault())
    val days = remember(visibleMonth) { buildMonthCells(visibleMonth) }
    val today = LocalDate.now()
    val dayCellShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreviousMonth, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = stringResource(R.string.common_close),
                        tint = RecordPrimaryText
                    )
                }
                Text(
                    text = visibleMonth.format(monthFormatter),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onMonthTitleClick),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RecordPrimaryText,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onNextMonth, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = stringResource(R.string.common_close),
                        tint = RecordPrimaryText
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekLabels().forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RecordSecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isCurrentMonth = date?.month == visibleMonth.month
                        val calendarBadgeType = date?.let(calendarBadgeForDate) ?: CalendarBadgeType.None

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.92f)
                                .clickable(enabled = date != null) {
                                    if (date != null) onSelectDate(date)
                                },
                            shape = dayCellShape,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) RecordAccentSurface else RecordCardColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .border(
                                                width = 2.dp,
                                                color = CalendarBadgeBlue,
                                                shape = dayCellShape
                                            )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 6.dp, bottom = 10.dp)
                                ) {
                                    Text(
                                        text = date?.dayOfMonth?.toString().orEmpty(),
                                        modifier = Modifier.align(Alignment.TopCenter),
                                        fontSize = 13.sp,
                                        color = if (isCurrentMonth) RecordPrimaryText else RecordSecondaryText,
                                        textAlign = TextAlign.Center
                                    )
                                    CalendarBadge(
                                        calendarBadgeType = calendarBadgeType,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarBadge(
    calendarBadgeType: CalendarBadgeType,
    modifier: Modifier = Modifier
) {
    val badgeShape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .size(12.dp)
            .background(Color.Transparent, badgeShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .background(calendarBadgeType.topLeftColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .background(calendarBadgeType.topRightColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .background(calendarBadgeType.bottomLeftColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .background(calendarBadgeType.bottomRightColor)
        )
    }
}

@Composable
private fun TwoByTwoBadge(
    topLeft: Color,
    topRight: Color,
    bottomLeft: Color,
    bottomRight: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(topLeft, RoundedCornerShape(topStart = 3.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(topRight, RoundedCornerShape(topEnd = 3.dp))
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(bottomLeft, RoundedCornerShape(bottomStart = 3.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(bottomRight, RoundedCornerShape(bottomEnd = 3.dp))
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    selectedDate: LocalDate,
    regularSummaryText: String,
    irregularSummaryText: String,
    onEditClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SummaryLine(stringResource(R.string.work_record_home_summary_date), selectedDate.format(dateFormatter))
                SummaryLine(stringResource(R.string.work_record_home_summary_regular), regularSummaryText)
                SummaryLine(stringResource(R.string.work_record_home_summary_irregular), irregularSummaryText)
            }
            TextButton(onClick = onEditClick, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.work_record_home_edit),
                    tint = RecordPrimaryText,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RecordPrimaryText)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RecordPrimaryText)
    }
}

@Composable
private fun WorkRecordDetailContent(
    innerPadding: PaddingValues,
    selectedDate: LocalDate,
    listItems: List<TaskWorkRecordItem>,
    recentRecordItems: List<TaskWorkRecordItem>,
    workName: String,
    workInputMode: WorkInputMode,
    referenceText: String,
    cycleNumberText: String,
    cycleUnitText: String,
    commentText: String,
    selectedStatus: RecordStatusOption,
    attachmentItems: List<TaskWorkRecordAttachmentItem>,
    isSaveEnabled: Boolean,
    isWorkFieldSelectionEnabled: Boolean,
    onWorkFieldClick: () -> Unit,
    onWorkNameChange: (String) -> Unit,
    onWorkNameFocusChanged: (Boolean) -> Unit,
    onReferenceChange: (String) -> Unit,
    onCycleNumberChange: (String) -> Unit,
    onCycleUnitChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onStatusChange: (RecordStatusOption) -> Unit,
    onAttachmentAddClick: () -> Unit,
    onAttachmentPreviewClick: (TaskWorkRecordAttachmentItem) -> Unit,
    onAttachmentDeleteClick: (TaskWorkRecordAttachmentItem) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRecentRecordClick: () -> Unit,
    onPdfClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleteEnabled: Boolean,
    onRecordClick: (TaskWorkRecordItem) -> Unit,
    onRecordLongClick: (TaskWorkRecordItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RecordBackground)
            .padding(innerPadding)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WorkRecordActionRow(
                isSaveEnabled = isSaveEnabled,
                onNewRecordClick = onEditClick,
                onSaveClick = onSaveClick
            )
        }
        item {
            WorkRecordEditorCard(
                workName = workName,
                workInputMode = workInputMode,
                referenceText = referenceText,
                cycleNumberText = cycleNumberText,
                cycleUnitText = cycleUnitText,
                commentText = commentText,
                selectedStatus = selectedStatus,
                attachmentItems = attachmentItems,
                onWorkFieldClick = onWorkFieldClick,
                isWorkFieldSelectionEnabled = isWorkFieldSelectionEnabled,
                onWorkNameChange = onWorkNameChange,
                onWorkNameFocusChanged = onWorkNameFocusChanged,
                onReferenceChange = onReferenceChange,
                onCycleNumberChange = onCycleNumberChange,
                onCycleUnitChange = onCycleUnitChange,
                onCommentChange = onCommentChange,
                onStatusChange = onStatusChange,
                onAttachmentAddClick = onAttachmentAddClick,
                onAttachmentPreviewClick = onAttachmentPreviewClick,
                onAttachmentDeleteClick = onAttachmentDeleteClick
            )
        }
        item {
            PdfDeleteCard(
                onPdfClick = onPdfClick,
                onDeleteClick = onDeleteClick,
                isDeleteEnabled = isDeleteEnabled
            )
        }
        item {
            RecentRecordCard(
                recentRecordItems = recentRecordItems,
                onClick = onRecentRecordClick
            )
        }
        item { TodayWorkListHeader(selectedDate = selectedDate) }
        if (listItems.isEmpty()) {
            item { EmptyWorkListCard() }
        } else {
            items(listItems, key = { it.id }) { record ->
                WorkRecordListRow(
                    record = record,
                    onClick = { onRecordClick(record) },
                    onLongClick = { onRecordLongClick(record) }
                )
            }
        }
    }
}

@Composable
private fun WorkRecordActionRow(
    isSaveEnabled: Boolean,
    onNewRecordClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onNewRecordClick, modifier = Modifier.size(52.dp)) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.work_detail_new_record),
                tint = RecordPrimaryText,
                modifier = Modifier.size(48.dp)
            )
        }
        TextButton(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = stringResource(R.string.work_detail_save),
                tint = if (isSaveEnabled) RecordPrimaryText else RecordSecondaryText.copy(alpha = 0.55f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

private data class RecordEditorSnapshot(
    val workInputMode: WorkInputMode,
    val workName: String,
    val referenceText: String,
    val cycleNumberText: String,
    val cycleUnitText: String,
    val selectedStatus: WorkBadgeType,
    val commentText: String,
    val attachmentKeys: List<String>
)

@Composable
private fun WorkRecordEditorCard(
    workName: String,
    workInputMode: WorkInputMode,
    referenceText: String,
    cycleNumberText: String,
    cycleUnitText: String,
    commentText: String,
    selectedStatus: RecordStatusOption,
    attachmentItems: List<TaskWorkRecordAttachmentItem>,
    isWorkFieldSelectionEnabled: Boolean,
    onWorkFieldClick: () -> Unit,
    onWorkNameChange: (String) -> Unit,
    onWorkNameFocusChanged: (Boolean) -> Unit,
    onReferenceChange: (String) -> Unit,
    onCycleNumberChange: (String) -> Unit,
    onCycleUnitChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onStatusChange: (RecordStatusOption) -> Unit,
    onAttachmentAddClick: () -> Unit,
    onAttachmentPreviewClick: (TaskWorkRecordAttachmentItem) -> Unit,
    onAttachmentDeleteClick: (TaskWorkRecordAttachmentItem) -> Unit
) {
    val context = LocalContext.current
    var cycleUnitExpanded by remember { mutableStateOf(false) }
    val cycleUnitOptions = remember {
        listOf(
            CycleUnitMenuOption(
                shortLabel = context.getString(R.string.cycle_unit_day_short),
                fullLabel = context.getString(R.string.cycle_unit_day)
            ),
            CycleUnitMenuOption(
                shortLabel = context.getString(R.string.cycle_unit_week_short),
                fullLabel = context.getString(R.string.cycle_unit_week)
            ),
            CycleUnitMenuOption(
                shortLabel = context.getString(R.string.cycle_unit_month_short),
                fullLabel = context.getString(R.string.cycle_unit_month)
            ),
            CycleUnitMenuOption(
                shortLabel = context.getString(R.string.cycle_unit_year_short),
                fullLabel = context.getString(R.string.cycle_unit_year)
            )
        )
    }
    val statusOptions = remember(workInputMode) { statusOptionsFor(workInputMode) }
    val isPresetLocked = workInputMode == WorkInputMode.REGULAR

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FieldLabel(stringResource(R.string.work_detail_field_work))
            if (workInputMode == WorkInputMode.IRREGULAR) {
                OutlinedTextField(
                    value = workName,
                    onValueChange = onWorkNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onWorkNameFocusChanged(it.isFocused) },
                    shape = RoundedCornerShape(18.dp),
                    colors = taskOutlinedTextFieldColors(RecordPrimaryText, RecordSecondaryText),
                    singleLine = false,
                    minLines = 1
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = isWorkFieldSelectionEnabled,
                            onClick = onWorkFieldClick
                        )
                ) {
                    OutlinedTextField(
                        value = workName,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(18.dp),
                        colors = taskOutlinedTextFieldColors(RecordPrimaryText, RecordSecondaryText),
                        singleLine = false,
                        minLines = 1
                    )
                }
            }

            FieldLabel(stringResource(R.string.work_detail_field_reference))
            OutlinedTextField(
                value = referenceText,
                onValueChange = onReferenceChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = taskOutlinedTextFieldColors(RecordPrimaryText, RecordSecondaryText),
                singleLine = false,
                minLines = 1,
                enabled = !isPresetLocked
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(0.4f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CenterLabel(stringResource(R.string.work_detail_field_cycle))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cycleNumberText,
                            onValueChange = {
                                val digitsOnly = it.filter { ch -> ch.isDigit() }.take(2)
                                onCycleNumberChange(digitsOnly)
                            },
                            modifier = Modifier.weight(0.32f),
                            shape = RoundedCornerShape(18.dp),
                            colors = taskOutlinedTextFieldColors(RecordPrimaryText, RecordSecondaryText),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                            enabled = !isPresetLocked
                        )
                        Box(modifier = Modifier.weight(0.5f)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isPresetLocked) { cycleUnitExpanded = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = RecordBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cycleUnitText.ifBlank { cycleUnitOptions.first().shortLabel },
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        color = RecordPrimaryText
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowDropDown,
                                        contentDescription = null,
                                        tint = RecordPrimaryText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = cycleUnitExpanded,
                                onDismissRequest = { cycleUnitExpanded = false },
                                modifier = Modifier.background(RecordCardColor)
                            ) {
                                cycleUnitOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.fullLabel,
                                                color = RecordPrimaryText,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        onClick = {
                                            cycleUnitExpanded = false
                                            onCycleUnitChange(option.shortLabel)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CenterLabel(stringResource(R.string.work_detail_field_status))
                    StatusDropdown(
                        selected = selectedStatus,
                        options = statusOptions,
                        enabled = workInputMode != WorkInputMode.NONE,
                        onSelect = onStatusChange
                    )
                }
            }

            FieldLabel(stringResource(R.string.work_detail_field_comment))
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(18.dp),
                colors = taskOutlinedTextFieldColors(RecordPrimaryText, RecordSecondaryText)
            )

            FieldLabel(stringResource(R.string.work_detail_field_attachment))
            AttachmentRow(
                attachments = attachmentItems,
                onAddClick = onAttachmentAddClick,
                onPreviewClick = onAttachmentPreviewClick,
                onDeleteClick = onAttachmentDeleteClick
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RecordPrimaryText)
}

@Composable
private fun CenterLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = RecordPrimaryText
    )
}

@Composable
private fun StatusDropdown(
    selected: RecordStatusOption,
    options: List<RecordStatusOption>,
    enabled: Boolean,
    onSelect: (RecordStatusOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { expanded = true },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WorkBadge(workBadgeType = selected.workBadgeType, modifier = Modifier.size(18.dp))
                Text(
                    text = recordStatusLabel(selected.workBadgeType),
                    modifier = Modifier.padding(start = 8.dp),
                    color = RecordPrimaryText
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = if (enabled) RecordPrimaryText else RecordSecondaryText.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(RecordCardColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WorkBadge(workBadgeType = option.workBadgeType, modifier = Modifier.size(18.dp))
                            Text(
                                text = recordStatusLabel(option.workBadgeType),
                                modifier = Modifier.padding(start = 8.dp),
                                color = RecordPrimaryText
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun RecordStatusPickerDialog(
    record: TaskWorkRecordItem,
    onDismiss: () -> Unit,
    onSelect: (RecordStatusOption) -> Unit
) {
    val options = remember(record.recordType) { statusOptionsFor(record.recordType) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "상태 변경",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordPrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RecordBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkBadge(workBadgeType = option.workBadgeType, modifier = Modifier.size(18.dp))
                            Text(
                                text = recordStatusLabel(option.workBadgeType),
                                modifier = Modifier.padding(start = 8.dp),
                                color = RecordPrimaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentRow(
    attachments: List<TaskWorkRecordAttachmentItem>,
    onAddClick: () -> Unit,
    onPreviewClick: (TaskWorkRecordAttachmentItem) -> Unit,
    onDeleteClick: (TaskWorkRecordAttachmentItem) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columnCount = 4
        val spacing = 10.dp
        val itemCount = attachments.size + 1
        val rowCount = ((itemCount + columnCount - 1) / columnCount).coerceAtLeast(1)
        val cellSize = (maxWidth - spacing * (columnCount - 1)) / columnCount
        val gridHeight = cellSize * rowCount + spacing * (rowCount - 1)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            userScrollEnabled = false
        ) {
            items(attachments, key = { it.id }) { attachment ->
                AttachmentPreviewCard(
                    attachment = attachment,
                    onClick = { onPreviewClick(attachment) },
                    onDeleteClick = { onDeleteClick(attachment) }
                )
            }
            item(key = "attachment_add") {
                EmptyAttachmentCard(onClick = onAddClick)
            }
        }
    }
}

@Composable
private fun EmptyAttachmentCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordBackground)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.work_detail_attachment_add),
                tint = RecordSecondaryText,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentPreviewCard(
    modifier: Modifier = Modifier,
    attachment: TaskWorkRecordAttachmentItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDeleteClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordBackground)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (attachment.type == TaskWorkRecordAttachmentType.IMAGE) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(attachment.uri))
                        .size(200)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                VideoThumbnailCardContent(attachment = attachment)
            }
        }
    }
}

@Composable
private fun AttachmentTypePickerDialog(
    onDismiss: () -> Unit,
    onImageCaptureClick: () -> Unit,
    onImageClick: () -> Unit,
    onVideoCaptureClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.work_detail_attachment_picker_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordPrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                AttachmentTypeActionCard(
                    text = stringResource(R.string.work_detail_attachment_image_capture),
                    onClick = onImageCaptureClick
                )
                AttachmentTypeActionCard(
                    text = stringResource(R.string.work_detail_attachment_image),
                    onClick = onImageClick
                )
                AttachmentTypeActionCard(
                    text = stringResource(R.string.work_detail_attachment_video_capture),
                    onClick = onVideoCaptureClick
                )
                AttachmentTypeActionCard(
                    text = stringResource(R.string.work_detail_attachment_video),
                    onClick = onVideoClick
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = RecordSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentTypeActionCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordBackground)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            color = RecordPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VideoThumbnailCardContent(
    attachment: TaskWorkRecordAttachmentItem
) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, key1 = attachment.uri) {
        value = withContext(Dispatchers.IO) {
            createVideoThumbnailBitmap(context, Uri.parse(attachment.uri))
                ?.let(::resizeBitmapForThumbnail)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        thumbnail?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Box(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.14f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Videocam,
                contentDescription = stringResource(R.string.work_detail_attachment_video),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = attachment.displayName.ifBlank { stringResource(R.string.work_detail_attachment_video) },
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AttachmentActionDialog(
    attachment: TaskWorkRecordAttachmentItem,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = attachment.displayName.ifBlank { stringResource(R.string.work_detail_attachment_picker_title) },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = RecordPrimaryText,
                    fontWeight = FontWeight.Bold
                )
                AttachmentTypeActionCard(
                    text = stringResource(R.string.common_delete),
                    onClick = onDeleteClick
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.common_cancel), color = RecordSecondaryText)
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    attachment: TaskWorkRecordAttachmentItem,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.86f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(attachment.uri))
                    .size(1440)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun queryAttachmentDisplayName(
    context: android.content.Context,
    uri: Uri
): String {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex).orEmpty()
                else ""
            }.orEmpty()
    }.getOrDefault("")
}

private fun createAttachmentCaptureFile(
    context: android.content.Context,
    prefix: String,
    extension: String
): File {
    val directory = File(context.filesDir, "task_attachments").apply { mkdirs() }
    return File(directory, "${prefix}_${System.currentTimeMillis()}.$extension")
}

private fun createVideoThumbnailBitmap(
    context: android.content.Context,
    uri: Uri
): Bitmap? {
    return runCatching {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
    }.getOrNull()
}

private fun resizeBitmapForThumbnail(bitmap: Bitmap): Bitmap {
    val maxSide = 360
    val width = bitmap.width
    val height = bitmap.height
    val largest = maxOf(width, height)
    if (largest <= maxSide) return bitmap
    val scale = maxSide / largest.toFloat()
    val resized = Bitmap.createScaledBitmap(
        bitmap,
        (width * scale).toInt().coerceAtLeast(1),
        (height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (resized != bitmap && !bitmap.isRecycled) {
        bitmap.recycle()
    }
    return resized
}

private fun queryVideoDurationMs(
    context: android.content.Context,
    uri: Uri
): Long? {
    return runCatching {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        }
    }.getOrNull()
}

@Composable
private fun RecentRecordCard(
    recentRecordItems: List<TaskWorkRecordItem>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.work_detail_recent_record),
                color = RecordPrimaryText,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (recentRecordItems.isEmpty()) {
                    stringResource(R.string.work_detail_recent_record_none)
                } else {
                    recentRecordItems.first().recordDate.format(
                        DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
                    )
                },
                color = RecordSecondaryText
            )
        }
    }
}

@Composable
private fun PdfDeleteCard(
    onPdfClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleteEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPdfClick),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.work_detail_pdf_save),
                    modifier = Modifier.padding(start = 14.dp),
                    color = RecordPrimaryText,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = isDeleteEnabled, onClick = onDeleteClick),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.work_detail_delete),
                    modifier = Modifier.padding(end = 14.dp),
                    color = if (isDeleteEnabled) RecordPrimaryText else RecordSecondaryText.copy(alpha = 0.55f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecentRecordDialog(
    workName: String,
    recentRecordItems: List<TaskWorkRecordItem>,
    onDismiss: () -> Unit,
    onRecordClick: (Long) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.work_detail_recent_record),
                        color = RecordPrimaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.common_close),
                            color = RecordPrimaryText
                        )
                    }
                }

                if (workName.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RecordAccentSurface)
                    ) {
                        Text(
                            text = workName,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = RecordPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (recentRecordItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RecordBackground)
                    ) {
                        Text(
                            text = stringResource(R.string.work_detail_recent_record_none),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            color = RecordSecondaryText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentRecordItems, key = { it.id }) { recentRecord ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRecordClick(recentRecord.id) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = RecordBackground)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = recentRecord.recordDate.format(
                                                DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
                                            ),
                                            color = RecordPrimaryText,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            WorkBadge(
                                                workBadgeType = recentRecord.status.toWorkBadgeType(),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = recordStatusLabel(recentRecord.status.toWorkBadgeType()),
                                                modifier = Modifier.padding(start = 6.dp),
                                                color = RecordPrimaryText,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (recentRecord.comment.isNotBlank()) {
                                        Text(
                                            text = recentRecord.comment,
                                            color = RecordSecondaryText,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayWorkListHeader(selectedDate: LocalDate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.work_detail_today_list),
                    color = RecordPrimaryText,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())),
                modifier = Modifier.width(88.dp),
                color = RecordSecondaryText,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

private data class PdfMediaItem(
    val type: TaskWorkRecordAttachmentType,
    val uri: String
)

private data class PdfPagePlan(
    val showHeader: Boolean,
    val showBasicInfo: Boolean,
    val showComment: Boolean,
    val mediaItems: List<PdfMediaItem>
)

private const val PDF_LOG_TAG = "EOD_PDF"

private fun generateWorkRecordPdf(
    context: android.content.Context,
    vesselName: String,
    presetName: String,
    recordDate: LocalDate,
    workName: String,
    cycleNumberText: String,
    cycleUnitText: String,
    statusLabel: String,
    comment: String,
    attachments: List<TaskWorkRecordAttachmentItem>
): Result<Uri> {
    return runCatching {
        Log.d(PDF_LOG_TAG, "generate start attachments=${attachments.size} date=$recordDate")
        val mediaItems = attachments
            .asSequence()
            .filter { it.uri.isNotBlank() }
            .map { attachment -> PdfMediaItem(type = attachment.type, uri = attachment.uri) }
            .toList()
        val pagePlans = buildPdfPagePlans(
            comment = comment,
            mediaItems = mediaItems
        )
        val document = PdfDocument()
        try {
            pagePlans.forEachIndexed { index, plan ->
                Log.d(PDF_LOG_TAG, "page create page=${index + 1}/${pagePlans.size} media=${plan.mediaItems.size}")
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                renderPdfPage(
                    context = context,
                    canvas = canvas,
                    pageNumber = index + 1,
                    pageCount = pagePlans.size,
                    vesselName = vesselName,
                    presetName = presetName,
                    recordDate = recordDate,
                    workName = workName,
                    cycleNumberText = cycleNumberText,
                    cycleUnitText = cycleUnitText,
                    statusLabel = statusLabel,
                    comment = comment,
                    plan = plan
                )
                document.finishPage(page)
            }
            val resolver = context.contentResolver
            val displayName = buildPdfFileName(
                vesselName = vesselName,
                presetName = presetName,
                workName = workName,
                recordDate = recordDate
            )
            Log.d(PDF_LOG_TAG, "write output name=$displayName")
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/EoD")
                }
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create PDF output.")
            resolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            } ?: throw IOException("Failed to open output stream.")
            Log.d(PDF_LOG_TAG, "generate success uri=$uri")
            uri
        } finally {
            document.close()
        }
    }.onFailure {
        Log.e(PDF_LOG_TAG, "generate failed", it)
    }
}

private fun buildPdfPagePlans(
    comment: String,
    mediaItems: List<PdfMediaItem>
): List<PdfPagePlan> {
    val plans = mutableListOf<PdfPagePlan>()
    val firstPageCapacity = if (mediaItems.isEmpty()) 0 else 4
    plans += PdfPagePlan(
        showHeader = true,
        showBasicInfo = true,
        showComment = comment.isNotBlank(),
        mediaItems = mediaItems.take(firstPageCapacity)
    )
    var remaining = mediaItems.drop(firstPageCapacity)
    while (remaining.isNotEmpty()) {
        plans += PdfPagePlan(
            showHeader = true,
            showBasicInfo = false,
            showComment = false,
            mediaItems = remaining.take(4)
        )
        remaining = remaining.drop(4)
    }
    return plans
}

private fun renderPdfPage(
    context: android.content.Context,
    canvas: android.graphics.Canvas,
    pageNumber: Int,
    pageCount: Int,
    vesselName: String,
    presetName: String,
    recordDate: LocalDate,
    workName: String,
    cycleNumberText: String,
    cycleUnitText: String,
    statusLabel: String,
    comment: String,
    plan: PdfPagePlan
) {
    val pageWidth = 595f
    val pageHeight = 842f
    val marginHorizontal = 32f
    val marginTop = 40f
    val marginBottom = 40f
    val contentWidth = pageWidth - (marginHorizontal * 2)
    val sectionGap = 16f
    val itemGap = 8f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = 20f
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = 14f
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = 12f
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = 12f
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#666666")
        textSize = 10f
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#DDDDDD")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#EAF4FF")
    }
    val pageText = "$pageNumber / $pageCount"

    canvas.drawColor(AndroidColor.WHITE)
    var currentY = marginTop

    if (plan.showHeader) {
        val headerHeight = 80f
        canvas.drawRoundRect(
            RectF(marginHorizontal, currentY, pageWidth - marginHorizontal, currentY + headerHeight),
            16f,
            16f,
            bluePaint
        )
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.eod_home_logo)
        logo?.let {
            val logoRect = RectF(marginHorizontal + 12f, currentY + 20f, marginHorizontal + 52f, currentY + 60f)
            drawCircularBitmap(canvas, it, logoRect)
            if (!it.isRecycled) it.recycle()
        }
        canvas.drawText(
            context.getString(R.string.work_detail_pdf_title),
            pageWidth / 2f - (titlePaint.measureText(context.getString(R.string.work_detail_pdf_title)) / 2f),
            currentY + 47f,
            titlePaint
        )
        val headerRightX = pageWidth - marginHorizontal - 12f
        canvas.drawText(vesselName, headerRightX - subPaint.measureText(vesselName), currentY + 28f, subPaint)
        canvas.drawText(presetName, headerRightX - subPaint.measureText(presetName), currentY + 44f, subPaint)
        val dateText = recordDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        canvas.drawText(dateText, headerRightX - subPaint.measureText(dateText), currentY + 60f, subPaint)
        canvas.drawLine(marginHorizontal, currentY + headerHeight, pageWidth - marginHorizontal, currentY + headerHeight, linePaint)
        currentY += headerHeight + sectionGap
    }

    if (plan.showBasicInfo) {
        canvas.drawText(context.getString(R.string.work_detail_pdf_section_basic), marginHorizontal, currentY, sectionPaint)
        currentY += 20f
        val labelWidth = 64f
        val infoRows = listOf(
            context.getString(R.string.work_detail_pdf_label_work_name) to workName,
            context.getString(R.string.work_detail_pdf_label_status) to statusLabel,
            context.getString(R.string.work_detail_pdf_label_cycle) to buildPdfCycleText(context, cycleNumberText, cycleUnitText)
        )
        infoRows.forEach { (label, value) ->
            canvas.drawText(label, marginHorizontal, currentY, bodyPaint)
            canvas.drawText(":", marginHorizontal + labelWidth, currentY, bodyPaint)
            canvas.drawText(value, marginHorizontal + labelWidth + 14f, currentY, bodyBoldPaint)
            currentY += 20f
        }
        currentY += sectionGap - 4f
    }

    if (plan.showComment && comment.isNotBlank()) {
        canvas.drawText(context.getString(R.string.work_detail_pdf_section_comment), marginHorizontal, currentY, sectionPaint)
        currentY += 20f
        wrapPdfText(comment, bodyPaint, contentWidth, 4).forEach { line ->
            canvas.drawText(line, marginHorizontal, currentY, bodyPaint)
            currentY += bodyPaint.textSize * 1.4f
        }
        currentY += sectionGap - 4f
    }

    if (plan.mediaItems.isNotEmpty()) {
        val imageItems = plan.mediaItems.filter { it.type == TaskWorkRecordAttachmentType.IMAGE }
        val videoItems = plan.mediaItems.filter { it.type == TaskWorkRecordAttachmentType.VIDEO }
        if (imageItems.isNotEmpty()) {
            canvas.drawText(context.getString(R.string.work_detail_pdf_section_photo), marginHorizontal, currentY, sectionPaint)
            currentY += 20f
            currentY = drawPdfMediaGrid(
                context = context,
                canvas = canvas,
                startY = currentY,
                pageHeight = pageHeight,
                marginHorizontal = marginHorizontal,
                bottomMargin = marginBottom,
                contentWidth = contentWidth,
                items = imageItems,
                linePaint = linePaint
            )
            currentY += sectionGap - 4f
        }
        if (videoItems.isNotEmpty()) {
            canvas.drawText(context.getString(R.string.work_detail_pdf_section_video), marginHorizontal, currentY, sectionPaint)
            currentY += 20f
            drawPdfMediaGrid(
                context = context,
                canvas = canvas,
                startY = currentY,
                pageHeight = pageHeight,
                marginHorizontal = marginHorizontal,
                bottomMargin = marginBottom,
                contentWidth = contentWidth,
                items = videoItems,
                linePaint = linePaint,
                drawPlayOverlay = true
            )
        }
    }

    canvas.drawText(
        pageText,
        pageWidth / 2f - (subPaint.measureText(pageText) / 2f),
        pageHeight - marginBottom / 2f,
        subPaint
    )
}

private fun drawPdfMediaGrid(
    context: android.content.Context,
    canvas: android.graphics.Canvas,
    startY: Float,
    pageHeight: Float,
    marginHorizontal: Float,
    bottomMargin: Float,
    contentWidth: Float,
    items: List<PdfMediaItem>,
    linePaint: Paint,
    drawPlayOverlay: Boolean = false
): Float {
    val gap = 8f
    val cellWidth = (contentWidth - gap) / 2f
    val cellHeight = 180f
    var currentY = startY
    items.chunked(2).forEach { rowItems ->
        if (currentY + cellHeight > pageHeight - bottomMargin - 20f) return@forEach
        rowItems.forEachIndexed { index, item ->
            val left = marginHorizontal + (index * (cellWidth + gap))
            val rect = RectF(left, currentY, left + cellWidth, currentY + cellHeight)
            canvas.drawRoundRect(rect, 12f, 12f, linePaint)
            val bitmap = runCatching { loadPdfBitmap(context, item) }
                .onFailure { Log.e(PDF_LOG_TAG, "load media failed type=${item.type} uri=${item.uri}", it) }
                .getOrNull()
            if (bitmap != null) {
                try {
                    drawBitmapFit(canvas, bitmap, rect)
                    if (drawPlayOverlay) {
                        drawPlayIcon(canvas, rect)
                    }
                } finally {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
        }
        currentY += cellHeight + gap
    }
    return currentY
}

private fun drawBitmapFit(canvas: android.graphics.Canvas, bitmap: Bitmap, rect: RectF) {
    val scale = minOf(rect.width() / bitmap.width.toFloat(), rect.height() / bitmap.height.toFloat())
    val drawWidth = bitmap.width * scale
    val drawHeight = bitmap.height * scale
    val left = rect.left + (rect.width() - drawWidth) / 2f
    val top = rect.top + (rect.height() - drawHeight) / 2f
    val destination = RectF(left, top, left + drawWidth, top + drawHeight)
    canvas.drawBitmap(bitmap, null, destination, null)
}

private fun drawCircularBitmap(canvas: android.graphics.Canvas, bitmap: Bitmap, rect: RectF) {
    val path = android.graphics.Path().apply {
        addOval(rect, android.graphics.Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(path)
    drawBitmapFit(canvas, bitmap, rect)
    canvas.restore()
}

private fun drawPlayIcon(canvas: android.graphics.Canvas, rect: RectF) {
    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(150, 0, 0, 0)
    }
    val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
    }
    val centerX = rect.centerX()
    val centerY = rect.centerY()
    canvas.drawCircle(centerX, centerY, 20f, overlayPaint)
    val path = android.graphics.Path().apply {
        moveTo(centerX - 6f, centerY - 10f)
        lineTo(centerX - 6f, centerY + 10f)
        lineTo(centerX + 10f, centerY)
        close()
    }
    canvas.drawPath(path, trianglePaint)
}

private fun wrapPdfText(
    text: String,
    paint: Paint,
    maxWidth: Float,
    maxLines: Int
): List<String> {
    if (text.isBlank()) return emptyList()
    val words = text.replace("\n", " ").split(" ")
    val lines = mutableListOf<String>()
    var current = ""
    words.forEach { word ->
        val candidate = if (current.isBlank()) word else "$current $word"
        if (paint.measureText(candidate) <= maxWidth) {
            current = candidate
        } else {
            if (current.isNotBlank()) lines += current
            current = word
        }
        if (lines.size == maxLines) return lines
    }
    if (current.isNotBlank() && lines.size < maxLines) lines += current
    return lines.take(maxLines).let { built ->
        if (built.size == maxLines && words.joinToString(" ").length > built.joinToString(" ").length) {
            built.dropLast(1) + "${built.last().take(maxOf(0, built.last().length - 3))}..."
        } else {
            built
        }
    }
}

private fun buildPdfCycleText(
    context: android.content.Context,
    cycleNumberText: String,
    cycleUnitText: String
): String {
    val unitLabel = when (cycleUnitText.trim().uppercase(Locale.getDefault())) {
        "D" -> context.getString(R.string.task_alarm_cycle_day)
        "W" -> context.getString(R.string.task_alarm_cycle_week)
        "M" -> context.getString(R.string.task_alarm_cycle_month)
        "Y" -> context.getString(R.string.task_alarm_cycle_year)
        else -> cycleUnitText
    }
    return listOf(cycleNumberText.trim(), unitLabel.trim()).filter { it.isNotBlank() }.joinToString(" ")
}

private fun loadBitmapForPdf(context: android.content.Context, uri: Uri): Bitmap? {
    return runCatching {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }
        val targetSize = 800
        val sampleSize = calculateInSampleSize(
            width = boundsOptions.outWidth,
            height = boundsOptions.outHeight,
            reqWidth = targetSize,
            reqHeight = targetSize
        )
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }?.let(::resizeBitmapForPdf)
    }.onFailure {
        Log.e(PDF_LOG_TAG, "loadBitmapForPdf failed uri=$uri", it)
    }.getOrNull()
}

private fun loadPdfBitmap(
    context: android.content.Context,
    item: PdfMediaItem
): Bitmap? {
    return when (item.type) {
        TaskWorkRecordAttachmentType.IMAGE -> loadBitmapForPdf(context, Uri.parse(item.uri))
        TaskWorkRecordAttachmentType.VIDEO -> {
            createVideoThumbnailBitmap(context, Uri.parse(item.uri))?.let(::resizeBitmapForPdf)
        }
    }
}

private fun resizeBitmapForPdf(bitmap: Bitmap): Bitmap {
    val maxSide = 800
    val width = bitmap.width
    val height = bitmap.height
    val largest = maxOf(width, height)
    if (largest <= maxSide) return bitmap
    val scale = maxSide / largest.toFloat()
    val resized = Bitmap.createScaledBitmap(
        bitmap,
        (width * scale).toInt(),
        (height * scale).toInt(),
        true
    )
    if (resized != bitmap && !bitmap.isRecycled) {
        bitmap.recycle()
    }
    return resized
}

private fun calculateInSampleSize(
    width: Int,
    height: Int,
    reqWidth: Int,
    reqHeight: Int
): Int {
    if (width <= 0 || height <= 0) return 1
    var inSampleSize = 1
    var halfWidth = width / 2
    var halfHeight = height / 2
    while ((halfWidth / inSampleSize) >= reqWidth && (halfHeight / inSampleSize) >= reqHeight) {
        inSampleSize *= 2
    }
    return inSampleSize.coerceAtLeast(1)
}

private fun buildPdfFileName(
    vesselName: String,
    presetName: String,
    workName: String,
    recordDate: LocalDate
): String {
    val dateText = recordDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val middle = workName.ifBlank { "record" }.take(18)
    return listOf(
        vesselName.ifBlank { "vessel" }.sanitizePdfFileNamePart().take(20),
        presetName.ifBlank { "preset" }.sanitizePdfFileNamePart().take(20),
        "업무기록",
        middle.sanitizePdfFileNamePart(),
        dateText
    ).filter { it.isNotBlank() }.joinToString("_") + ".pdf"
}

private fun String.sanitizePdfFileNamePart(): String {
    return replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
}

@Composable
private fun WorkTypePickerDialog(
    onDismiss: () -> Unit,
    onRegularClick: () -> Unit,
    onIrregularClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.work_detail_type_select_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RecordPrimaryText
                )
                PickerButton(text = stringResource(R.string.work_detail_type_regular), onClick = onRegularClick)
                PickerButton(text = stringResource(R.string.work_detail_type_irregular), onClick = onIrregularClick)
            }
        }
    }
}

@Composable
private fun PickerButton(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordAccentSurface)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            color = RecordPrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PresetWorkPickerDialog(
    presetName: String,
    works: List<TaskPresetWorkItem>,
    selectedWorkId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    onApply: (TaskPresetWorkItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredWorks = remember(works, query) {
        val normalized = query.trim()
        if (normalized.isBlank()) works
        else works.filter { it.name.contains(normalized, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = RecordCardColor)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.size(42.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.preset_picker_close),
                            tint = RecordPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Text(
                        text = presetName,
                        color = RecordPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    TextButton(
                        onClick = { works.firstOrNull { it.id == selectedWorkId }?.let(onApply) },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(R.string.preset_picker_apply),
                            tint = RecordPrimaryText,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.work_record_home_search_hint),
                            color = RecordSecondaryText
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = RecordPrimaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.preset_picker_title),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = RecordPrimaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.work_detail_field_cycle),
                        modifier = Modifier.width(70.dp),
                        textAlign = TextAlign.Center,
                        color = RecordPrimaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (filteredWorks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.preset_picker_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = RecordSecondaryText
                    )
                } else {
                    filteredWorks.forEach { work ->
                        val selected = selectedWorkId == work.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(work.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Color(0xFFD9E6FF) else RecordBackground
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = work.name,
                                    modifier = Modifier.weight(1f),
                                    color = RecordPrimaryText,
                                    maxLines = 2
                                )
                                Text(
                                    text = cycleShortLabel(work),
                                    modifier = Modifier.width(70.dp),
                                    textAlign = TextAlign.Center,
                                    color = RecordPrimaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                RadioIndicator(selected = selected)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onApply: (YearMonth) -> Unit
) {
    var selectedYear by remember(initialMonth) { mutableStateOf(initialMonth.year) }
    var selectedMonth by remember(initialMonth) { mutableStateOf(initialMonth.monthValue) }
    val years = remember { (2019..2035).toList() }
    val months = remember { (1..12).toList() }

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
            colors = CardDefaults.cardColors(containerColor = RecordCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "${selectedYear}.${selectedMonth.toString().padStart(2, '0')}",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RecordPrimaryText,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(color = RecordDivider)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WheelColumn(
                        values = years.map { it.toString() },
                        selectedIndex = years.indexOf(selectedYear),
                        onSelect = { selectedYear = years[it] },
                        modifier = Modifier.weight(1f)
                    )
                    WheelColumn(
                        values = months.map { it.toString().padStart(2, '0') },
                        selectedIndex = months.indexOf(selectedMonth),
                        onSelect = { selectedMonth = months[it] },
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = RecordDivider)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.common_cancel), color = RecordSecondaryText)
                    }
                    TextButton(onClick = { onApply(YearMonth.of(selectedYear, selectedMonth)) }) {
                        Text(
                            text = stringResource(R.string.preset_picker_apply),
                            color = RecordPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WheelColumn(
    values: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RecordBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                count = values.size,
                key = { index -> index }
            ) { index ->
                val selected = index == selectedIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clickable { onSelect(index) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) RecordAccentSurface else RecordCardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = values[index],
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        fontSize = 18.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) RecordPrimaryText else RecordSecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkListHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
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
                modifier = Modifier.weight(4.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RecordSecondaryText,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.work_detail_field_status),
                modifier = Modifier.weight(2.2f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RecordSecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WorkListRow(
    name: String,
    status: WorkListStatusUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(4.5f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RecordPrimaryText,
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier.weight(2.2f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkBadge(workBadgeType = status.workBadgeType, modifier = Modifier.size(16.dp))
                Text(
                    text = recordStatusLabel(status.workBadgeType),
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 11.sp,
                    color = RecordPrimaryText
                )
            }
        }
    }
}

@Composable
private fun RadioIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(
                width = 2.dp,
                color = if (selected) RecordPrimaryText else RecordSecondaryText,
                shape = RoundedCornerShape(999.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(RecordPrimaryText, RoundedCornerShape(999.dp))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkRecordListRow(
    record: TaskWorkRecordItem,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val isRegular = record.recordType == TaskWorkRecordType.REGULAR
    val containerColor = if (isRegular) RecordAccentSurface else Color(0xFFFFF1E6)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongClick?.invoke() }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(4.4f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.workName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RecordPrimaryText,
                    maxLines = 2
                )
                if (record.comment.isNotBlank()) {
                    Text(
                        text = record.comment,
                        color = RecordSecondaryText,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }
            Row(
                modifier = Modifier.weight(2.3f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkBadge(
                    workBadgeType = record.status.toWorkBadgeType(),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = recordStatusLabel(record.status.toWorkBadgeType()),
                    modifier = Modifier.padding(start = 8.dp),
                    color = RecordPrimaryText,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun WorkBadge(
    workBadgeType: WorkBadgeType,
    modifier: Modifier = Modifier
) {
    TwoByTwoBadge(
        topLeft = workBadgeType.topLeftColor,
        topRight = workBadgeType.topRightColor,
        bottomLeft = workBadgeType.bottomLeftColor,
        bottomRight = workBadgeType.bottomRightColor,
        modifier = modifier
    )
}

@Composable
private fun EmptyWorkListCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RecordCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = stringResource(R.string.work_record_home_empty),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 13.sp,
            color = RecordSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

private fun buildCalendarBadgeType(
    date: LocalDate,
    records: List<TaskWorkRecordItem>
): CalendarBadgeType {
    val hasRegular = records.any { it.recordType == TaskWorkRecordType.REGULAR }
    val hasIrregular = records.any { it.recordType == TaskWorkRecordType.IRREGULAR }
    val hasDelayed = records.any {
        it.status == TaskWorkRecordStatus.RegularDelayed ||
            it.status == TaskWorkRecordStatus.NonRegularDelayed
    }
    return when {
        hasRegular && hasIrregular && hasDelayed -> CalendarBadgeType.RegularNonRegularDelayed
        hasRegular && hasDelayed -> CalendarBadgeType.RegularDelayed
        hasIrregular && hasDelayed -> CalendarBadgeType.NonRegularDelayed
        hasRegular && hasIrregular -> CalendarBadgeType.RegularNonRegular
        hasRegular -> CalendarBadgeType.Regular
        hasIrregular -> CalendarBadgeType.NonRegular
        else -> CalendarBadgeType.None
    }
}

private fun weekLabels(): List<String> {
    val first = DayOfWeek.MONDAY
    return (0..6).map { first.plus(it.toLong()) }
        .map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3) }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val result = mutableListOf<LocalDate?>()

    repeat(startOffset) { result += null }
    repeat(month.lengthOfMonth()) { day -> result += month.atDay(day + 1) }
    while (result.size % 7 != 0) result += null
    return result
}

private fun cycleShortLabel(work: TaskPresetWorkItem): String {
    return "${work.cycleNumber} ${cycleUnitShortLabel(work)}"
}

private fun cycleUnitShortLabel(work: TaskPresetWorkItem): String {
    return when (work.cycleUnit.name) {
        "DAY" -> "D"
        "WEEK" -> "W"
        "MONTH" -> "M"
        else -> "Y"
    }
}

private data class CycleUnitMenuOption(
    val shortLabel: String,
    val fullLabel: String
)

private data class WorkListStatusUi(
    val workBadgeType: WorkBadgeType,
    val textResId: Int,
    val isRegular: Boolean,
    val isIrregular: Boolean,
    val isDelayed: Boolean
)

private enum class CalendarBadgeType(
    val topLeftColor: Color,
    val topRightColor: Color,
    val bottomLeftColor: Color,
    val bottomRightColor: Color
) {
    None(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
    Regular(CalendarBadgeBlue, CalendarBadgeBlue, Color.Transparent, Color.Transparent),
    NonRegular(Color.Transparent, Color.Transparent, CalendarBadgeOrange, CalendarBadgeOrange),
    RegularNonRegular(CalendarBadgeBlue, CalendarBadgeBlue, CalendarBadgeOrange, CalendarBadgeOrange),
    RegularDelayed(CalendarBadgeBlue, CalendarBadgeRed, Color.Transparent, CalendarBadgeRed),
    NonRegularDelayed(Color.Transparent, CalendarBadgeRed, CalendarBadgeOrange, CalendarBadgeRed),
    RegularNonRegularDelayed(CalendarBadgeBlue, CalendarBadgeRed, CalendarBadgeOrange, CalendarBadgeRed)
}

private enum class WorkBadgeType(
    val topLeftColor: Color,
    val topRightColor: Color,
    val bottomLeftColor: Color,
    val bottomRightColor: Color
) {
    RegularPlanned(WorkBadgeBaseGray, WorkBadgeGreen, WorkBadgeBaseGray, WorkBadgeBaseGray),
    RegularDelayed(WorkBadgeBaseGray, WorkBadgeRed, WorkBadgeBaseGray, WorkBadgeRed),
    RegularDone(WorkBadgeBaseGray, WorkBadgeBlue, WorkBadgeBlue, WorkBadgeBlue),
    NonRegularPlanned(WorkBadgeOrange, WorkBadgeGreen, WorkBadgeBaseGray, WorkBadgeBaseGray),
    NonRegularDelayed(WorkBadgeOrange, WorkBadgeRed, WorkBadgeBaseGray, WorkBadgeRed),
    NonRegularDone(WorkBadgeOrange, WorkBadgeBlue, WorkBadgeBlue, WorkBadgeBlue)
}

private fun WorkBadgeType.toTaskWorkRecordStatus(): TaskWorkRecordStatus {
    return when (this) {
        WorkBadgeType.RegularPlanned -> TaskWorkRecordStatus.RegularPlanned
        WorkBadgeType.RegularDelayed -> TaskWorkRecordStatus.RegularDelayed
        WorkBadgeType.RegularDone -> TaskWorkRecordStatus.RegularDone
        WorkBadgeType.NonRegularPlanned -> TaskWorkRecordStatus.NonRegularPlanned
        WorkBadgeType.NonRegularDelayed -> TaskWorkRecordStatus.NonRegularDelayed
        WorkBadgeType.NonRegularDone -> TaskWorkRecordStatus.NonRegularDone
    }
}

private fun TaskWorkRecordStatus.toWorkBadgeType(): WorkBadgeType {
    return when (this) {
        TaskWorkRecordStatus.RegularPlanned -> WorkBadgeType.RegularPlanned
        TaskWorkRecordStatus.RegularDelayed -> WorkBadgeType.RegularDelayed
        TaskWorkRecordStatus.RegularDone -> WorkBadgeType.RegularDone
        TaskWorkRecordStatus.NonRegularPlanned -> WorkBadgeType.NonRegularPlanned
        TaskWorkRecordStatus.NonRegularDelayed -> WorkBadgeType.NonRegularDelayed
        TaskWorkRecordStatus.NonRegularDone -> WorkBadgeType.NonRegularDone
    }
}

private fun TaskWorkRecordStatus.toRecordStatusOption(): RecordStatusOption {
    return defaultStatusOptions().first { it.workBadgeType == this.toWorkBadgeType() }
}

private fun WorkBadgeType.toStatusTextResId(): Int {
    return when (this) {
        WorkBadgeType.RegularPlanned,
        WorkBadgeType.NonRegularPlanned -> R.string.badge_status_planned
        WorkBadgeType.RegularDelayed,
        WorkBadgeType.NonRegularDelayed -> R.string.badge_status_delayed
        WorkBadgeType.RegularDone,
        WorkBadgeType.NonRegularDone -> R.string.badge_status_completed
    }
}

private fun statusOptionsFor(workInputMode: WorkInputMode): List<RecordStatusOption> {
    return when (workInputMode) {
        WorkInputMode.REGULAR -> defaultStatusOptions().filter {
            it.workBadgeType == WorkBadgeType.RegularPlanned ||
                it.workBadgeType == WorkBadgeType.RegularDelayed ||
                it.workBadgeType == WorkBadgeType.RegularDone
        }
        WorkInputMode.IRREGULAR -> defaultStatusOptions().filter {
            it.workBadgeType == WorkBadgeType.NonRegularPlanned ||
                it.workBadgeType == WorkBadgeType.NonRegularDelayed ||
                it.workBadgeType == WorkBadgeType.NonRegularDone
        }
        WorkInputMode.NONE -> emptyList()
    }
}

private fun statusOptionsFor(recordType: TaskWorkRecordType): List<RecordStatusOption> {
    return when (recordType) {
        TaskWorkRecordType.REGULAR -> statusOptionsFor(WorkInputMode.REGULAR)
        TaskWorkRecordType.IRREGULAR -> statusOptionsFor(WorkInputMode.IRREGULAR)
    }
}

@Composable
private fun buildStatusSummaryText(records: List<TaskWorkRecordItem>): String {
    val plannedCount = records.count {
        it.status == TaskWorkRecordStatus.RegularPlanned ||
            it.status == TaskWorkRecordStatus.NonRegularPlanned
    }
    val delayedCount = records.count {
        it.status == TaskWorkRecordStatus.RegularDelayed ||
            it.status == TaskWorkRecordStatus.NonRegularDelayed
    }
    val doneCount = records.count {
        it.status == TaskWorkRecordStatus.RegularDone ||
            it.status == TaskWorkRecordStatus.NonRegularDone
    }

    return listOfNotNull(
        if (plannedCount > 0) "${stringResource(R.string.badge_status_planned)}-$plannedCount" else null,
        if (delayedCount > 0) "${stringResource(R.string.badge_status_delayed)}-$delayedCount" else null,
        if (doneCount > 0) "${stringResource(R.string.badge_status_completed)}-$doneCount" else null
    ).joinToString(" ")
}

private enum class WorkInputMode {
    NONE,
    REGULAR,
    IRREGULAR
}

private enum class WorkRecordListFilter {
    TODAY,
    ALL
}

private data class RecordStatusOption(
    val labelResId: Int,
    val workBadgeType: WorkBadgeType
)

@Composable
private fun recordStatusLabel(workBadgeType: WorkBadgeType): String {
    val type = when (workBadgeType) {
        WorkBadgeType.RegularPlanned,
        WorkBadgeType.RegularDelayed,
        WorkBadgeType.RegularDone -> stringResource(R.string.work_detail_type_regular)
        WorkBadgeType.NonRegularPlanned,
        WorkBadgeType.NonRegularDelayed,
        WorkBadgeType.NonRegularDone -> stringResource(R.string.work_detail_type_irregular)
    }
    val status = when (workBadgeType) {
        WorkBadgeType.RegularPlanned,
        WorkBadgeType.NonRegularPlanned -> stringResource(R.string.badge_status_planned)
        WorkBadgeType.RegularDelayed,
        WorkBadgeType.NonRegularDelayed -> stringResource(R.string.badge_status_delayed)
        WorkBadgeType.RegularDone,
        WorkBadgeType.NonRegularDone -> stringResource(R.string.badge_status_completed)
    }
    return "$type - $status"
}

private fun defaultStatusOptions(): List<RecordStatusOption> {
    return listOf(
        RecordStatusOption(R.string.badge_status_planned, WorkBadgeType.RegularPlanned),
        RecordStatusOption(R.string.badge_status_delayed, WorkBadgeType.RegularDelayed),
        RecordStatusOption(R.string.badge_status_completed, WorkBadgeType.RegularDone),
        RecordStatusOption(R.string.badge_status_planned, WorkBadgeType.NonRegularPlanned),
        RecordStatusOption(R.string.badge_status_delayed, WorkBadgeType.NonRegularDelayed),
        RecordStatusOption(R.string.badge_status_completed, WorkBadgeType.NonRegularDone)
    )
}
