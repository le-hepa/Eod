package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel

private data class AlarmWorkRowItem(
    val workName: String,
    val reference: String,
    val cycle: String
)

@Composable
fun TaskAlarmScreen(
    onBackClick: () -> Unit
) {
    val viewModel: TaskTopViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val activeVessel = uiState.vesselItems.firstOrNull { it.enabled }
    val activePreset = uiState.presetGroups.firstOrNull { it.name == activeVessel?.presetName }
    val alarmWorkItems = activePreset?.works?.map {
        AlarmWorkRowItem(
            workName = it.workName,
            reference = it.reference,
            cycle = it.cycle
        )
    }.orEmpty()

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
                    fontSize = 22.sp,
                    color = Color(0xFF123A73)
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
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "알림목록",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF123A73)
                            )
                            Text(
                                text = if (activeVessel != null) {
                                    "현재 활성 선박: ${activeVessel.name}"
                                } else {
                                    "현재 활성 선박이 없습니다"
                                },
                                fontSize = 14.sp,
                                color = Color(0xFF5D7598)
                            )
                        }

                        Switch(
                            checked = uiState.alarmEnabled,
                            onCheckedChange = viewModel::onAlarmToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E6CEB),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD7E3F2)
                            )
                        )
                    }
                }
            }

            if (!uiState.alarmEnabled) {
                item {
                    EmptyAlarmCard(
                        message = "현재 활성화된 알림이 없습니다"
                    )
                }
            } else if (activeVessel == null) {
                item {
                    EmptyAlarmCard(
                        message = "현재 활성 선박이 없습니다"
                    )
                }
            } else if (alarmWorkItems.isEmpty()) {
                item {
                    EmptyAlarmCard(
                        message = "현재 활성화된 알림이 없습니다"
                    )
                }
            } else {
                items(alarmWorkItems) { work ->
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
                                text = work.workName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF123A73)
                            )

                            Text(
                                text = "참조정보",
                                fontSize = 13.sp,
                                color = Color(0xFF7A8EA9)
                            )
                            Text(
                                text = work.reference.ifBlank { "-" },
                                fontSize = 14.sp,
                                color = Color(0xFF123A73)
                            )

                            Text(
                                text = "업무주기",
                                fontSize = 13.sp,
                                color = Color(0xFF7A8EA9)
                            )
                            Text(
                                text = work.cycle.ifBlank { "-" },
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

@Composable
private fun EmptyAlarmCard(
    message: String
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Text(
            text = message,
            fontSize = 15.sp,
            color = Color(0xFF5D7598),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
        )
    }
}