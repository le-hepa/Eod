package com.gomgom.eod.feature.cargoinfo.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomgom.eod.R
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSearchMode
import com.gomgom.eod.feature.portinfo.screens.PortHeaderAction
import com.gomgom.eod.feature.portinfo.screens.PortHeaderIcons
import com.gomgom.eod.feature.portinfo.screens.PortSearchCardSection
import com.gomgom.eod.feature.portinfo.screens.RecordListSection
import com.gomgom.eod.feature.portinfo.screens.SearchSection
import com.gomgom.eod.feature.task.screens.TaskHamburgerMenuButton

@Composable
fun CargoInfoTopPlaceholderScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.port_info_back),
                            tint = Color(0xFF123A73)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.cargo_info_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TaskHamburgerMenuButton(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it },
                        iconTint = Color(0xFF123A73),
                        menuBackgroundColor = Color.White,
                        dividerColor = Color(0xFFE3ECF8),
                        textColor = Color(0xFF123A73),
                        onHomeClick = onHomeClick,
                        onKorClick = onKorClick,
                        onEngClick = onEngClick,
                        onAppInfoClick = {},
                        onGuideClick = onGuideClick,
                        onContactClick = onContactClick,
                        onExitClick = onExitClick
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 5.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchSection(
                searchEnabled = false,
                canSave = false,
                stateLabel = stringResource(R.string.cargo_info_placeholder_state),
                topSearchQuery = "",
                searchPlaceholder = stringResource(R.string.cargo_info_placeholder_search),
                showFilterButton = false,
                searchMode = PortToolSearchMode.FULL_TEXT,
                pendingLiveSearchCount = 0,
                showLiveSearchHeader = false,
                isTopSearchFocused = false,
                searchFocusRequester = searchFocusRequester,
                showFilterMenu = false,
                onTopSearchFocusChanged = {},
                onTopSearchQueryChange = {},
                onFilterMenuExpandedChange = {},
                onSearchModeToggle = {},
                onDataManageClick = {},
                leftAction = PortHeaderAction(
                    icon = PortHeaderIcons.Search,
                    contentDescription = stringResource(R.string.port_info_search),
                    enabled = false,
                    onClick = {}
                ),
                rightActions = listOf(
                    PortHeaderAction(
                        icon = PortHeaderIcons.New,
                        contentDescription = stringResource(R.string.port_info_add_record),
                        enabled = false,
                        onClick = {}
                    )
                ),
                onLiveSearchConfirm = {},
                onLiveSearchHeaderDismiss = {},
                onSearchBarClick = {}
            )

            PortSearchCardSection(
                editable = false,
                countryValue = "",
                portValue = "",
                unlocodeValue = "",
                onEditorFieldFocusChange = { _, _ -> },
                onCountryChange = {},
                onPortChange = {},
                onUnlocodeChange = {}
            )

            RecordListSection(
                modifier = Modifier.weight(1f),
                items = emptyList(),
                onRecordClick = {}
            )
        }
    }
}
