package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.gomgom.eod.R

private val GuideBackground = Color(0xFFF5F8FC)
private val GuideSurface = Color.White
private val GuideDrawerSurface = Color(0xFFF8FBFF)
private val GuidePrimaryText = Color(0xFF123A73)
private val GuideSecondaryText = Color(0xFF6E85A3)
private val GuideDivider = Color(0xFFDCE5F0)
private val GuideBullet = Color(0xFF2E6CEB)

@Composable
fun TaskGuideScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }

    val blocks = remember { guideBlocks() }
    val blockIndexById = remember(blocks) {
        blocks.mapIndexedNotNull { index, block ->
            block.anchorId?.let { it to index }
        }.toMap()
    }
    val tocItems = remember { guideTocItems() }

    if (appInfoVisible) {
        GuideInfoDialog(onDismiss = { appInfoVisible = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = GuideDrawerSurface
            ) {
                Text(
                    text = stringResource(R.string.task_guide_toc_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    color = GuidePrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = GuideDivider)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(tocItems) { _, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        blockIndexById[item.anchorId]?.let { index ->
                                            listState.animateScrollToItem(index)
                                        }
                                        drawerState.close()
                                    }
                                }
                                .padding(start = if (item.isChild) 28.dp else 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isChild) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(GuideBullet, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                text = stringResource(item.labelRes),
                                color = GuidePrimaryText,
                                fontSize = if (item.isChild) 14.sp else 15.sp,
                                fontWeight = if (item.isChild) FontWeight.Medium else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            containerColor = GuideBackground,
            topBar = {
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
                                tint = GuidePrimaryText,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.home_guide_title),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GuidePrimaryText,
                            textAlign = TextAlign.Center
                        )
                    }

                    TaskHamburgerMenuButton(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it },
                        iconTint = GuidePrimaryText,
                        menuBackgroundColor = GuideSurface,
                        dividerColor = GuideDivider,
                        textColor = GuidePrimaryText,
                        onHomeClick = onHomeClick,
                        onKorClick = onKorClick,
                        onEngClick = onEngClick,
                        onAppInfoClick = { appInfoVisible = true },
                        onGuideClick = {},
                        onContactClick = onContactClick,
                        onExitClick = onExitClick
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(GuideBackground)
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Text(
                                text = stringResource(R.string.task_guide_toc_title),
                                color = GuidePrimaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                itemsIndexed(blocks, key = { _, block -> block.key }) { _, block ->
                    when (block) {
                        is GuideBlock.SectionHeader -> {
                            Text(
                                text = stringResource(block.titleRes),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                color = GuidePrimaryText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        is GuideBlock.Topic -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(block.titleRes),
                                    color = GuidePrimaryText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(block.bodyRes),
                                    color = GuidePrimaryText,
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideInfoDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = GuideSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_app_info_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = GuidePrimaryText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = GuideDivider)
                Text(
                    text = stringResource(R.string.home_app_info_name_value),
                    color = GuidePrimaryText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_app_info_version_value),
                    color = GuideSecondaryText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.home_app_info_confirm),
                            color = GuidePrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private sealed class GuideBlock(
    val key: String,
    val anchorId: String?
) {
    class SectionHeader(
        key: String,
        val titleRes: Int,
        anchorId: String? = null
    ) : GuideBlock(key, anchorId)

    class Topic(
        key: String,
        val titleRes: Int,
        val bodyRes: Int,
        anchorId: String
    ) : GuideBlock(key, anchorId)
}

private data class GuideTocItem(
    val labelRes: Int,
    val anchorId: String,
    val isChild: Boolean
)

private fun guideBlocks(): List<GuideBlock> = listOf(
    GuideBlock.SectionHeader("features_header", R.string.task_guide_section_features),
    GuideBlock.Topic("app_intro", R.string.task_guide_app_intro_title, R.string.task_guide_app_intro_body, "app_intro"),
    GuideBlock.Topic("main_features", R.string.task_guide_main_features_title, R.string.task_guide_main_features_body, "main_features"),
    GuideBlock.Topic("feature_home", R.string.task_guide_feature_home_title, R.string.task_guide_feature_home_body, "feature_home"),
    GuideBlock.Topic("feature_task_main", R.string.task_guide_feature_task_main_title, R.string.task_guide_feature_task_main_body, "feature_task_main"),
    GuideBlock.Topic("feature_preset", R.string.task_guide_feature_preset_title, R.string.task_guide_feature_preset_body, "feature_preset"),
    GuideBlock.Topic("feature_regular_work", R.string.task_guide_feature_regular_work_title, R.string.task_guide_feature_regular_work_body, "feature_regular_work"),
    GuideBlock.Topic("feature_vessel", R.string.task_guide_feature_vessel_title, R.string.task_guide_feature_vessel_body, "feature_vessel"),
    GuideBlock.Topic("feature_record_home", R.string.task_guide_feature_record_home_title, R.string.task_guide_feature_record_home_body, "feature_record_home"),
    GuideBlock.Topic("feature_record_write", R.string.task_guide_feature_record_write_title, R.string.task_guide_feature_record_write_body, "feature_record_write"),
    GuideBlock.Topic("feature_recent", R.string.task_guide_feature_recent_title, R.string.task_guide_feature_recent_body, "feature_recent"),
    GuideBlock.Topic("feature_record_list", R.string.task_guide_feature_record_list_title, R.string.task_guide_feature_record_list_body, "feature_record_list"),
    GuideBlock.Topic("feature_alarm_list", R.string.task_guide_feature_alarm_list_title, R.string.task_guide_feature_alarm_list_body, "feature_alarm_list"),
    GuideBlock.Topic("feature_alarm_time", R.string.task_guide_feature_alarm_time_title, R.string.task_guide_feature_alarm_time_body, "feature_alarm_time"),
    GuideBlock.Topic("feature_data_manage", R.string.task_guide_feature_data_manage_title, R.string.task_guide_feature_data_manage_body, "feature_data_manage"),
    GuideBlock.Topic("feature_attachment", R.string.task_guide_feature_attachment_title, R.string.task_guide_feature_attachment_body, "feature_attachment"),
    GuideBlock.Topic("alarm_explanation", R.string.task_guide_alarm_explanation_title, R.string.task_guide_alarm_explanation_body, "alarm_explanation"),
    GuideBlock.Topic("screen_summary", R.string.task_guide_screen_summary_title, R.string.task_guide_screen_summary_body, "screen_summary"),
    GuideBlock.SectionHeader("howto_header", R.string.task_guide_section_howto),
    GuideBlock.Topic("howto_first", R.string.task_guide_howto_first_title, R.string.task_guide_howto_first_body, "howto_first"),
    GuideBlock.Topic("howto_regular", R.string.task_guide_howto_regular_title, R.string.task_guide_howto_regular_body, "howto_regular"),
    GuideBlock.Topic("howto_write", R.string.task_guide_howto_write_title, R.string.task_guide_howto_write_body, "howto_write"),
    GuideBlock.Topic("howto_date", R.string.task_guide_howto_date_title, R.string.task_guide_howto_date_body, "howto_date"),
    GuideBlock.Topic("howto_edit", R.string.task_guide_howto_edit_title, R.string.task_guide_howto_edit_body, "howto_edit"),
    GuideBlock.Topic("howto_alarm", R.string.task_guide_howto_alarm_title, R.string.task_guide_howto_alarm_body, "howto_alarm"),
    GuideBlock.Topic("howto_irregular", R.string.task_guide_howto_irregular_title, R.string.task_guide_howto_irregular_body, "howto_irregular"),
    GuideBlock.Topic("howto_data_manage", R.string.task_guide_howto_data_manage_title, R.string.task_guide_howto_data_manage_body, "howto_data_manage"),
    GuideBlock.Topic("howto_flow", R.string.task_guide_howto_flow_title, R.string.task_guide_howto_flow_body, "howto_flow")
)

private fun guideTocItems(): List<GuideTocItem> = listOf(
    GuideTocItem(R.string.task_guide_app_intro_title, "app_intro", false),
    GuideTocItem(R.string.task_guide_main_features_title, "main_features", false),
    GuideTocItem(R.string.task_guide_feature_home_title, "feature_home", true),
    GuideTocItem(R.string.task_guide_feature_task_main_title, "feature_task_main", true),
    GuideTocItem(R.string.task_guide_feature_preset_title, "feature_preset", true),
    GuideTocItem(R.string.task_guide_feature_regular_work_title, "feature_regular_work", true),
    GuideTocItem(R.string.task_guide_feature_vessel_title, "feature_vessel", true),
    GuideTocItem(R.string.task_guide_feature_record_home_title, "feature_record_home", true),
    GuideTocItem(R.string.task_guide_feature_record_write_title, "feature_record_write", true),
    GuideTocItem(R.string.task_guide_feature_recent_title, "feature_recent", true),
    GuideTocItem(R.string.task_guide_feature_record_list_title, "feature_record_list", true),
    GuideTocItem(R.string.task_guide_feature_alarm_list_title, "feature_alarm_list", true),
    GuideTocItem(R.string.task_guide_feature_alarm_time_title, "feature_alarm_time", true),
    GuideTocItem(R.string.task_guide_feature_data_manage_title, "feature_data_manage", true),
    GuideTocItem(R.string.task_guide_feature_attachment_title, "feature_attachment", true),
    GuideTocItem(R.string.task_guide_alarm_explanation_title, "alarm_explanation", false),
    GuideTocItem(R.string.task_guide_screen_summary_title, "screen_summary", false),
    GuideTocItem(R.string.task_guide_section_howto, "howto_first", false),
    GuideTocItem(R.string.task_guide_howto_first_title, "howto_first", true),
    GuideTocItem(R.string.task_guide_howto_regular_title, "howto_regular", true),
    GuideTocItem(R.string.task_guide_howto_write_title, "howto_write", true),
    GuideTocItem(R.string.task_guide_howto_date_title, "howto_date", true),
    GuideTocItem(R.string.task_guide_howto_edit_title, "howto_edit", true),
    GuideTocItem(R.string.task_guide_howto_alarm_title, "howto_alarm", true),
    GuideTocItem(R.string.task_guide_howto_irregular_title, "howto_irregular", true),
    GuideTocItem(R.string.task_guide_howto_data_manage_title, "howto_data_manage", true),
    GuideTocItem(R.string.task_guide_howto_flow_title, "howto_flow", true)
)
