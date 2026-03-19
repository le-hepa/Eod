package com.gomgom.eod.feature.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class GuideItem(
    val title: String,
    val body: String
)

@Composable
fun HomeGuideScreen(
    onBackClick: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }

    val guideItems = remember {
        listOf(
            GuideItem("홈", "업무관리, 항만정보, 화물정보로 진입하는 시작 화면입니다."),
            GuideItem("업무관리", "업무 프리셋, 알림 목록, 선박 목록, 선박 추가를 관리합니다."),
            GuideItem("업무 프리셋", "상위 프리셋을 만들고 활성화한 뒤 하위 업무 프리셋을 관리합니다."),
            GuideItem("하위 업무 프리셋", "업무명, 참조정보, 업무주기를 저장하고 수정합니다."),
            GuideItem("알림 목록", "활성 선박 기준 업무 목록과 알림 상태를 확인합니다."),
            GuideItem("업무기록 홈", "달력 기준으로 날짜별 업무를 확인하는 화면입니다."),
            GuideItem("업무상세기록", "정기/비정기 업무 기록과 코멘트, 첨부를 작성하는 화면입니다.")
        )
    }

    val filteredItems = remember(keyword, guideItems) {
        if (keyword.isBlank()) {
            guideItems
        } else {
            guideItems.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                        it.body.contains(keyword, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 10.dp, top = 8.dp)
            ) {
                Text(
                    text = "←",
                    color = Color(0xFF123A73),
                    fontSize = 22.sp
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("가이드 검색") }
                )
            }

            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Text(
                            text = "검색 결과 없음",
                            modifier = Modifier.padding(18.dp),
                            fontSize = 16.sp,
                            color = Color(0xFF123A73)
                        )
                    }
                }
            } else {
                items(filteredItems) { item ->
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF123A73)
                            )
                            Text(
                                text = item.body,
                                fontSize = 14.sp,
                                color = Color(0xFF123A73)
                            )
                        }
                    }
                }
            }
        }
    }
}