package com.gomgom.eod.feature.portinfo.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.portinfo.screens.PortInfoTopScreen
import com.gomgom.eod.feature.portinfo.viewmodel.PortInfoTopViewModel

@Composable
fun PortInfoToolWrapper(
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit
) {
    val viewModel: PortInfoTopViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    PortInfoTopScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRecordClick = onRecordClick,
        onAddRecordClick = onAddRecordClick,
        onTabSelect = viewModel::selectTab,
        onSearchKeywordChange = viewModel::updateSearchKeyword
    )
}