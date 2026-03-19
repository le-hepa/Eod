package com.gomgom.eod.feature.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gomgom.eod.R
import com.gomgom.eod.core.common.AutoResizeText

private data class HomeCardItem(
    val title: String,
    val onClick: () -> Unit
)

private val HomeBackground = Color(0xFFF5F8FC)
private val HomeCardColor = Color.White
private val HomeAccentSurface = Color(0xFFEAF2FF)
private val HomePrimaryText = Color(0xFF123A73)
private val HomeSecondaryText = Color(0xFF6E85A3)
private val HomeDivider = Color(0xFFDCE5F0)

@Composable
fun EodHomeScreen(
    onTaskClick: () -> Unit,
    onPortInfoClick: () -> Unit,
    onCargoInfoClick: () -> Unit,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var appInfoVisible by remember { mutableStateOf(false) }
    var guideVisible by remember { mutableStateOf(false) }

    val cards = listOf(
        HomeCardItem(stringResource(R.string.home_card_task), onTaskClick),
        HomeCardItem(stringResource(R.string.home_card_port), onPortInfoClick),
        HomeCardItem(stringResource(R.string.home_card_cargo), onCargoInfoClick),
        HomeCardItem(stringResource(R.string.home_card_coming_soon), onClick = {})
    )

    if (appInfoVisible) {
        HomePopupFrame(
            title = stringResource(R.string.home_app_info_title),
            confirmText = stringResource(R.string.home_app_info_confirm),
            onDismiss = { appInfoVisible = false }
        ) {
            PopupInfoRow(
                label = stringResource(R.string.home_app_info_name_label),
                value = stringResource(R.string.home_app_info_name_value)
            )
            PopupInfoRow(
                label = stringResource(R.string.home_app_info_version_label),
                value = stringResource(R.string.home_app_info_version_value)
            )
        }
    }

    if (guideVisible) {
        HomePopupFrame(
            title = stringResource(R.string.home_guide_title),
            confirmText = stringResource(R.string.home_guide_close),
            onDismiss = { guideVisible = false }
        ) {
            PopupGuideLine(text = stringResource(R.string.home_guide_search_hint))
            PopupGuideLine(text = stringResource(R.string.task_top_title))
            PopupGuideLine(text = stringResource(R.string.task_preset_top_title))
            PopupGuideLine(text = stringResource(R.string.alert_list_title))
            PopupGuideLine(text = stringResource(R.string.work_record_home_title))
            PopupGuideLine(text = stringResource(R.string.work_detail_new_record))
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = HomeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground)
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(HomeAccentSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomePrimaryText
                    )
                }

                Text(
                    text = stringResource(R.string.home_title_app),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomePrimaryText
                )

                Box {
                    TextButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.common_home),
                            tint = HomePrimaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(HomeCardColor)
                    ) {
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_home)) },
                            onClick = {
                                menuExpanded = false
                                onHomeClick()
                            }
                        )
                        HorizontalDivider(color = HomeDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_kor)) },
                            onClick = {
                                menuExpanded = false
                                onKorClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_eng)) },
                            onClick = {
                                menuExpanded = false
                                onEngClick()
                            }
                        )
                        HorizontalDivider(color = HomeDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_app_info)) },
                            onClick = {
                                menuExpanded = false
                                appInfoVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_guide)) },
                            onClick = {
                                menuExpanded = false
                                guideVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_contact)) },
                            onClick = {
                                menuExpanded = false
                                onContactClick()
                            }
                        )
                        HorizontalDivider(color = HomeDivider)
                        DropdownMenuItem(
                            text = { MenuText(stringResource(R.string.home_menu_exit)) },
                            onClick = {
                                menuExpanded = false
                                onExitClick()
                            }
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(cards.size) { index ->
                    val item = cards[index]
                    HomeEntryCard(
                        title = item.title,
                        onClick = item.onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuText(
    text: String
) {
    Text(
        text = text,
        color = HomePrimaryText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun HomeEntryCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HomeCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HomeCardColor)
                .padding(vertical = 42.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoResizeText(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomePrimaryText,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                minFontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HomePopupFrame(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
            colors = CardDefaults.cardColors(containerColor = HomeCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomePrimaryText
                )

                HorizontalDivider(color = HomeDivider)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )

                HorizontalDivider(color = HomeDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = confirmText,
                            color = HomePrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupInfoRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = HomeSecondaryText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = HomePrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PopupGuideLine(
    text: String
) {
    Text(
        text = "• $text",
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = HomePrimaryText
    )
}