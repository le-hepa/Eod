package com.gomgom.eod.feature.portinfo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomgom.eod.feature.portinfo.viewmodel.PortInfoSourceTab
import com.gomgom.eod.feature.portinfo.viewmodel.PortInfoTopUiState

@Composable
fun PortInfoTopScreen(
    uiState: PortInfoTopUiState,
    onBackClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit,
    onTabSelect: (PortInfoSourceTab) -> Unit,
    onSearchKeywordChange: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBackClick) {
                    Text(text = "←", fontSize = 22.sp, color = Color(0xFF123A73))
                }

                Text(
                    text = "항만정보",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123A73)
                )

                TextButton(onClick = onAddRecordClick) {
                    Text(text = "+", fontSize = 22.sp, color = Color(0xFF123A73))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PortInfoTabCard(
                        title = "내기록",
                        selected = uiState.selectedTab == PortInfoSourceTab.MY_RECORDS,
                        onClick = { onTabSelect(PortInfoSourceTab.MY_RECORDS) },
                        modifier = Modifier.weight(1f)
                    )
                    PortInfoTabCard(
                        title = "공유기록",
                        selected = uiState.selectedTab == PortInfoSourceTab.SHARED_RECORDS,
                        onClick = { onTabSelect(PortInfoSourceTab.SHARED_RECORDS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.searchKeyword,
                    onValueChange = onSearchKeywordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("검색") }
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.selectedTab == PortInfoSourceTab.MY_RECORDS) "내기록 목록" else "공유기록 목록",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF123A73)
                        )
                        Text(
                            text = "STEP 10에서 연결 예정",
                            fontSize = 15.sp,
                            color = Color(0xFF5D7598)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecordClick(0L) },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "기록 상세 진입 자리",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF123A73)
                        )
                        Text(
                            text = "국가 / 항만 / 부두 / 화물",
                            fontSize = 14.sp,
                            color = Color(0xFF5D7598)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortInfoTabCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF2E6CEB) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFF123A73),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        )
    }
}