package com.gomgom.eod.feature.task.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.feature.task.viewmodel.TaskTopViewModel

@Composable
fun TaskVesselDetailScreen(
    vesselId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TaskTopViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val vesselItem = uiState.vesselItems.firstOrNull { it.id == vesselId }

    var editName by remember(vesselId) { mutableStateOf("") }

    LaunchedEffect(vesselItem?.name) {
        editName = vesselItem?.name.orEmpty()
    }

    if (vesselItem == null) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F8FC))
                    .padding(innerPadding)
                    .padding(18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            text = "선박 정보 없음",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF123A73)
                        )
                        Text(
                            text = "삭제되었거나 존재하지 않는 선박입니다.",
                            fontSize = 15.sp,
                            color = Color(0xFF5D7598)
                        )
                    }
                }
            }
        }
        return
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
                    fontSize = 22.sp,
                    color = Color(0xFF123A73)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .padding(18.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "선박 상세",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )

                    Text(
                        text = "선박 ID: $vesselId",
                        fontSize = 14.sp,
                        color = Color(0xFF5D7598)
                    )

                    Text(
                        text = "적용 프리셋: ${vesselItem.presetName}",
                        fontSize = 14.sp,
                        color = Color(0xFF5D7598)
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("선박명") }
                    )

                    Button(
                        onClick = {
                            viewModel.updateVesselName(vesselId, editName)
                            Toast.makeText(context, "선박명이 변경되었습니다", Toast.LENGTH_SHORT).show()
                        },
                        enabled = editName.trim().isNotEmpty()
                    ) {
                        Text("선박명 저장")
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFD6E0EC)
                    )

                    Text(
                        text = "활성 상태",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF123A73)
                    )

                    Switch(
                        checked = vesselItem.enabled,
                        onCheckedChange = { checked ->
                            viewModel.onVesselToggle(vesselId, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2E6CEB),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD7E3F2)
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.deleteVessel(vesselId)
                            onBackClick()
                        }
                    ) {
                        Text("선박 삭제")
                    }
                }
            }

            DetailSectionCard(title = "업무") {
                Text("등록된 업무가 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }

            DetailSectionCard(title = "참조자료") {
                Text("등록된 참조자료가 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }

            DetailSectionCard(title = "주기") {
                Text("등록된 주기가 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }

            DetailSectionCard(title = "코멘트") {
                Text("등록된 코멘트가 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }

            DetailSectionCard(title = "첨부") {
                Text("등록된 첨부가 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }

            DetailSectionCard(title = "최근기록") {
                Text("최근기록이 없습니다.", fontSize = 15.sp, color = Color(0xFF5D7598))
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF123A73)
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFD6E0EC)
            )

            content()
        }
    }
}