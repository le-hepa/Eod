package com.gomgom.eod.feature.portinfo.porttool.wrapper

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeLookupRepository
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortEditorFocusField
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchDisplayMode
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchSourceField
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSearchMode
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolViewModel
import com.gomgom.eod.feature.portinfo.screens.PortRecordListItem
import com.gomgom.eod.feature.portinfo.screens.PortScreenOrigin
import com.gomgom.eod.feature.portinfo.screens.PortScreenState
import com.gomgom.eod.feature.portinfo.screens.PortInfoTopScreen

@Composable
fun PortToolWrapper(
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit
) {
    val context = LocalContext.current
    var activeSource by rememberSaveable { mutableStateOf(PortToolSource.LOCAL) }
    var screenState by remember { mutableStateOf(PortScreenState.LIST) }
    var recordOrigin by rememberSaveable { mutableStateOf(PortScreenOrigin.LIST) }
    var lastSearchModeName by rememberSaveable { mutableStateOf(PortToolSearchMode.COUNTRY_PORT.name) }
    var listCountryFilter by rememberSaveable(activeSource.name) { mutableStateOf("") }
    var listPortFilter by rememberSaveable(activeSource.name) { mutableStateOf("") }
    var listCodeFilter by rememberSaveable(activeSource.name) { mutableStateOf("") }
    PortToolSession.activeSource = activeSource

    val localRepository = remember(context) { createPortToolRepository(context, PortToolSource.LOCAL) }
    val sharedRepository = remember(context) { createPortToolRepository(context, PortToolSource.SHARED) }
    val lookupRepository = remember(context) { PortUnlocodeLookupRepository(context) }
    val repository = if (activeSource == PortToolSource.LOCAL) localRepository else sharedRepository

    val viewModel: PortToolViewModel = viewModel(
        key = "port-tool-${activeSource.name}",
        factory = portToolViewModelFactory(repository, lookupRepository)
    )
    LaunchedEffect(activeSource, viewModel) {
        viewModel.selectSource(activeSource)
        if (viewModel.hasPendingNewDraftForActiveSource()) {
            viewModel.restorePendingNewDraftOrOpenEmpty()
            screenState = PortScreenState.CREATE
        } else {
            viewModel.updateSearchQuery("")
            listCountryFilter = ""
            listPortFilter = ""
            listCodeFilter = ""
            viewModel.clearEditorState()
            screenState = PortScreenState.LIST
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val dbState by viewModel.dbState.collectAsState()
    val tempState by viewModel.tempState.collectAsState()
    val activeBundle = tempState.editorBundle ?: dbState.selectedRecord
    var arrivalDates by remember(activeSource) { mutableStateOf<Map<Long, String>>(emptyMap()) }

    LaunchedEffect(uiState.searchMode, screenState) {
        if (screenState != PortScreenState.LIST) {
            lastSearchModeName = uiState.searchMode.name
        }
    }

    LaunchedEffect(dbState.records, repository, screenState) {
        if (screenState != PortScreenState.LIST) return@LaunchedEffect
        arrivalDates = dbState.records.associate { record ->
            val arrivalDate = repository.getRecordBundle(record.id)
                ?.operations
                ?.firstOrNull { it.operationType == com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType.AGENT }
                ?.arrivalDate
                .orEmpty()
            record.id to arrivalDate
        }
    }

    val listItems = remember(
        dbState.records,
        arrivalDates,
        uiState.topSearchQuery,
        listCountryFilter,
        listPortFilter,
        listCodeFilter
    ) {
        dbState.records
            .asSequence()
            .filter { record ->
                val topQuery = uiState.topSearchQuery.trim().lowercase()
                val topPass = topQuery.isBlank() || listOf(
                    record.countryName,
                    record.countryCode,
                    record.portName,
                    record.unlocode,
                    record.berthName,
                    record.anchorageName,
                    record.cargoName
                ).joinToString(" ").lowercase().contains(topQuery)
                val countryPass = listCountryFilter.isBlank() ||
                    record.countryName.contains(listCountryFilter, ignoreCase = true) ||
                    record.countryCode.contains(listCountryFilter, ignoreCase = true)
                val portPass = listPortFilter.isBlank() ||
                    record.portName.contains(listPortFilter, ignoreCase = true)
                val codePass = listCodeFilter.isBlank() ||
                    record.unlocode.replace("-", "").contains(listCodeFilter.replace("-", ""), ignoreCase = true)
                topPass && countryPass && portPass && codePass
            }
            .sortedByDescending { it.updatedAt }
            .map {
                PortRecordListItem(
                    id = it.id,
                    country = it.countryName.ifBlank { it.countryCode },
                    port = it.portName,
                    berth = it.berthName,
                    anchorage = it.anchorageName,
                    cargo = it.cargoName,
                    arrivalDate = arrivalDates[it.id].orEmpty()
                )
            }
            .toList()
    }

    val hasEditableChanges = tempState.hasPendingChanges
    val canCreate = activeSource == PortToolSource.LOCAL
    val canEditRecord = activeSource == PortToolSource.LOCAL

    fun resetSearchScreen() {
        viewModel.updateSearchQuery("")
        viewModel.updateFieldSearchInput(PortSearchSourceField.COUNTRY, "")
        viewModel.updateFieldSearchInput(PortSearchSourceField.PORT, "")
        viewModel.updateFieldSearchInput(PortSearchSourceField.CODE, "")
        viewModel.openRecord(null)
        viewModel.updateSearchMode(runCatching { PortToolSearchMode.valueOf(lastSearchModeName) }.getOrDefault(PortToolSearchMode.COUNTRY_PORT))
    }

    fun resetListFilters() {
        listCountryFilter = ""
        listPortFilter = ""
        listCodeFilter = ""
    }

    PortInfoTopScreen(
        screenState = screenState,
        currentSource = activeSource,
        listItems = listItems,
        canCreate = canCreate,
        canEditRecord = canEditRecord,
        uiState = uiState,
        dbState = dbState,
        tempState = tempState,
        onBackClick = {
            when (screenState) {
                PortScreenState.LIST -> onBackClick()
                PortScreenState.SEARCH -> {
                    resetSearchScreen()
                    screenState = PortScreenState.LIST
                }
                PortScreenState.CREATE -> {
                    resetSearchScreen()
                    screenState = PortScreenState.SEARCH
                }
                PortScreenState.RECORD -> {
                    screenState = if (recordOrigin == PortScreenOrigin.LIST) PortScreenState.LIST else PortScreenState.SEARCH
                    if (recordOrigin == PortScreenOrigin.SEARCH) resetSearchScreen()
                }
                PortScreenState.EDIT -> {
                    screenState = if (recordOrigin == PortScreenOrigin.LIST) PortScreenState.LIST else PortScreenState.SEARCH
                    if (recordOrigin == PortScreenOrigin.SEARCH) resetSearchScreen()
                }
            }
        },
        onRecordClick = { recordId ->
            viewModel.openRecord(recordId)
            recordOrigin = if (screenState == PortScreenState.LIST) PortScreenOrigin.LIST else PortScreenOrigin.SEARCH
            screenState = PortScreenState.RECORD
        },
        onAddRecordClick = {
            viewModel.openRecord(null)
            screenState = PortScreenState.CREATE
        },
        onSaveClick = viewModel::saveCurrent,
        onEnterSearch = {
            resetSearchScreen()
            screenState = PortScreenState.SEARCH
        },
        onEnterList = {
            viewModel.prepareListMode()
            resetListFilters()
            viewModel.clearEditorState()
            screenState = PortScreenState.LIST
        },
        onEnterEdit = { screenState = PortScreenState.EDIT },
        onSaveComplete = {
            screenState = PortScreenState.RECORD
        },
        onDeleteCurrent = {
            val recordId = activeBundle?.record?.id ?: return@PortInfoTopScreen
            viewModel.delete(recordId)
            screenState = if (recordOrigin == PortScreenOrigin.LIST) PortScreenState.LIST else PortScreenState.SEARCH
            if (recordOrigin == PortScreenOrigin.SEARCH) resetSearchScreen()
        },
        onDiscardAndGoSearch = {
            resetSearchScreen()
            screenState = PortScreenState.SEARCH
        },
        onDiscardAndGoBack = {
            screenState = if (screenState == PortScreenState.CREATE) {
                resetSearchScreen()
                PortScreenState.SEARCH
            } else {
                if (recordOrigin == PortScreenOrigin.SEARCH) {
                    resetSearchScreen()
                    PortScreenState.SEARCH
                } else {
                    PortScreenState.LIST
                }
            }
        },
        onToggleVesselReporting = viewModel::toggleVesselReporting,
        onToggleAnchorage = viewModel::toggleAnchorage,
        onToggleBerth = viewModel::toggleBerth,
        onBundleChange = viewModel::updateEditorBundle,
        onEditorFieldFocusChange = viewModel::updateFieldFocus,
        onSearchResultSelect = { item ->
            viewModel.selectSearchResult(item)
            if (item.recordId != null) {
                recordOrigin = PortScreenOrigin.SEARCH
                screenState = PortScreenState.RECORD
            }
        },
        onAttachmentsSelected = viewModel::addAttachments,
        onAttachmentDelete = viewModel::removeAttachment,
        onSourceSelect = { source ->
            activeSource = source
        },
        onSearchQueryChange = viewModel::updateSearchQuery,
        onFieldSearchInputChange = viewModel::updateFieldSearchInput,
        onFormFieldSearchInputChange = viewModel::updateFormFieldSearchInput,
        listCountryFilter = listCountryFilter,
        listPortFilter = listPortFilter,
        listCodeFilter = listCodeFilter,
        onListCountryFilterChange = { listCountryFilter = it },
        onListPortFilterChange = { listPortFilter = it },
        onListCodeFilterChange = { listCodeFilter = it },
        onSearchDisplayModeChange = viewModel::updateSearchDisplayMode,
        onSearchModeChange = {
            viewModel.updateSearchMode(
                if (uiState.searchMode == PortToolSearchMode.COUNTRY_PORT) {
                    PortToolSearchMode.FULL_TEXT
                } else {
                    PortToolSearchMode.COUNTRY_PORT
                }
            )
            viewModel.search()
        },
        onExportJson = viewModel::exportJson,
        onExportCsv = viewModel::exportCsv,
        onImportJson = viewModel::importJson,
        onLiveSearchConfirm = viewModel::enableLiveSearch,
        onLiveSearchCancel = viewModel::dismissLiveSearchPrompt,
        onLiveSearchHeaderDismiss = viewModel::dismissLiveSearchHeader,
        hasEditableChanges = hasEditableChanges
    )
}
