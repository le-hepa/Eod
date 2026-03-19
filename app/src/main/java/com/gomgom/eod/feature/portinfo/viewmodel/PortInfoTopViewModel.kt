package com.gomgom.eod.feature.portinfo.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PortInfoTopViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PortInfoTopUiState())
    val uiState: StateFlow<PortInfoTopUiState> = _uiState.asStateFlow()

    fun selectTab(tab: PortInfoSourceTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateSearchKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(searchKeyword = keyword)
    }
}