package com.gomgom.eod.feature.portinfo.porttool.wrapper

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeLookupRepository
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSearchMode
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolViewModel
import com.gomgom.eod.feature.portinfo.screens.PortInfoTopScreen

@Composable
fun PortToolWrapper(
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit
) {
    val context = LocalContext.current
    var activeSource = rememberSaveable { PortToolSource.LOCAL }
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
        viewModel.openRecord(null)
    }
    val uiState by viewModel.uiState.collectAsState()
    val dbState by viewModel.dbState.collectAsState()
    val tempState by viewModel.tempState.collectAsState()

    PortInfoTopScreen(
        uiState = uiState,
        dbState = dbState,
        tempState = tempState,
        onBackClick = onBackClick,
        onRecordClick = { recordId ->
            viewModel.openRecord(recordId)
        },
        onAddRecordClick = {
            viewModel.openRecord(null)
        },
        onSaveClick = viewModel::saveCurrent,
        onToggleVesselReporting = viewModel::toggleVesselReporting,
        onToggleAnchorage = viewModel::toggleAnchorage,
        onToggleBerth = viewModel::toggleBerth,
        onBundleChange = viewModel::updateEditorBundle,
        onCountrySuggestionClick = viewModel::applyCountrySuggestion,
        onPortSuggestionClick = viewModel::applyPortSuggestion,
        onAttachmentsSelected = viewModel::addAttachments,
        onAttachmentDelete = viewModel::removeAttachment,
        onSourceSelect = { source ->
            activeSource = source
            viewModel.selectSource(source)
            viewModel.openRecord(null)
        },
        onCountryKeywordChange = { keyword ->
            viewModel.updateCountryKeyword(keyword)
        },
        onPortKeywordChange = { keyword ->
            viewModel.updatePortKeyword(keyword)
        },
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
        onLiveSearchHeaderDismiss = viewModel::dismissLiveSearchHeader
    )
}
