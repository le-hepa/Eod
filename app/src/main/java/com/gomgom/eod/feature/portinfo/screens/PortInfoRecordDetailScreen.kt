package com.gomgom.eod.feature.portinfo.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeLookupRepository
import com.gomgom.eod.feature.portinfo.porttool.wrapper.PortToolSession
import com.gomgom.eod.feature.portinfo.porttool.wrapper.createPortToolRepository
import com.gomgom.eod.feature.portinfo.porttool.wrapper.portToolViewModelFactory
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolViewModel

@Composable
fun PortInfoRecordDetailScreen(
    recordId: Long,
    onBackClick: () -> Unit,
    onAugmentClick: () -> Unit
) {
    val context = LocalContext.current
    val source = PortToolSession.activeSource
    val repository = remember(context, source) { createPortToolRepository(context, source) }
    val lookupRepository = remember(context) { PortUnlocodeLookupRepository(context) }
    val viewModel: PortToolViewModel = viewModel(
        key = "port-record-detail-${source.name}",
        factory = portToolViewModelFactory(repository, lookupRepository)
    )
    val dbState by viewModel.dbState.collectAsState()
    val tempState by viewModel.tempState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(recordId, source) {
        viewModel.selectSource(source)
        viewModel.openRecord(recordId)
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF123A73))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "항만정보",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123A73)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (source == PortToolSource.LOCAL) {
                    FilledTonalIconButton(onClick = onAugmentClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFF123A73))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Home") }, onClick = {
                        showMenu = false
                        onBackClick()
                    })
                    DropdownMenuItem(text = { Text("KOR") }, onClick = {
                        showMenu = false
                        AppLanguageManager.applyKor(context)
                        (context as? Activity)?.recreate()
                    })
                    DropdownMenuItem(text = { Text("ENG") }, onClick = {
                        showMenu = false
                        AppLanguageManager.applyEng(context)
                        (context as? Activity)?.recreate()
                    })
                    DropdownMenuItem(text = { Text("App Info") }, onClick = {
                        showMenu = false
                        Toast.makeText(context, "Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}", Toast.LENGTH_SHORT).show()
                    })
                    DropdownMenuItem(text = { Text("Contact") }, onClick = {
                        showMenu = false
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "EoD 앱 문의")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}\nAndroid Version: ${Build.VERSION.RELEASE}\nDevice Model: ${Build.MANUFACTURER} ${Build.MODEL}\n문의 내용:\n"
                            )
                        }
                        runCatching { context.startActivity(intent) }
                    })
                    DropdownMenuItem(text = { Text("Exit") }, onClick = {
                        showMenu = false
                        (context as? Activity)?.finish()
                    })
                }
            }
        }
    ) { innerPadding ->
        val bundle = dbState.selectedRecord ?: tempState.editorBundle
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (bundle != null) {
                PortInfoRecordContent(
                    editable = false,
                    bundle = bundle,
                    countrySuggestions = emptyList(),
                    portSuggestions = emptyList(),
                    isVesselReportingExpanded = tempState.isVesselReportingExpanded,
                    isAnchorageExpanded = tempState.isAnchorageExpanded,
                    isBerthExpanded = tempState.isBerthExpanded,
                    onToggleVesselReporting = viewModel::toggleVesselReporting,
                    onToggleAnchorage = viewModel::toggleAnchorage,
                    onToggleBerth = viewModel::toggleBerth,
                    onBundleChange = {},
                    onCountrySuggestionClick = {},
                    onPortSuggestionClick = {},
                    onAddAttachmentClick = {},
                    onDeleteAttachmentClick = {}
                )
            }
        }
    }
}
