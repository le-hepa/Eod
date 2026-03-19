package com.gomgom.eod.feature.portinfo.viewmodel

import com.gomgom.eod.feature.portinfo.entity.PortInfoRecord

enum class PortInfoSourceTab {
    MY_RECORDS,
    SHARED_RECORDS
}

data class PortInfoTopUiState(
    val selectedTab: PortInfoSourceTab = PortInfoSourceTab.MY_RECORDS,
    val searchKeyword: String = "",
    val records: List<PortInfoRecord> = emptyList()
)