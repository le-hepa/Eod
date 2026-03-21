package com.gomgom.eod.feature.portinfo.wrapper

import androidx.compose.runtime.Composable
import com.gomgom.eod.feature.portinfo.porttool.wrapper.PortToolWrapper

@Composable
fun PortInfoToolWrapper(
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit
) {
    PortToolWrapper(
        onBackClick = onBackClick,
        onRecordClick = onRecordClick,
        onAddRecordClick = onAddRecordClick
    )
}
