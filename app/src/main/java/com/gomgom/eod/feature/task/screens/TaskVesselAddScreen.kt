package com.gomgom.eod.feature.task.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.gomgom.eod.feature.task.viewmodel.TaskVesselAddViewModel

@Composable
fun TaskVesselAddScreen(
    onBackClick: () -> Unit,
    onGoPresetClick: () -> Unit,
    onVesselSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val addViewModel: TaskVesselAddViewModel = viewModel()
    val topViewModel: TaskTopViewModel = viewModel()
    val vesselName by addViewModel.vesselName.collectAsState()
    val uiState by topViewModel.uiState.collectAsState()

    var selectedPresetId by remember { mutableLongStateOf(-1L) }

    val enabledPresetGroups = uiState.presetGroups.filter { it.enabled && it.works.isNotEmpty() }
    val selectedPresetName = enabledPresetGroups.firstOrNull { it.id == selectedPresetId }?.name.orEmpty()

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
        androidx.compose.foundation.layout.Column(
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
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "선박 추가",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )

                    if (enabledPresetGroups.isEmpty()) {
                        Text(
                            text = "먼저 업무가 있는 활성 프리셋을 만들어 주세요",
                            fontSize = 15.sp,
                            color = Color(0xFF5D7598)
                        )

                        Button(onClick = onGoPresetClick) {
                            Text("프리셋 화면으로 이동")
                        }
                    } else {
                        OutlinedTextField(
                            value = vesselName,
                            onValueChange = addViewModel::onVesselNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("선박명") }
                        )

                        Text(
                            text = "적용 프리셋 선택",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF123A73)
                        )

                        enabledPresetGroups.forEach { preset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPresetId = preset.id },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            text = preset.name,
                                            fontSize = 15.sp,
                                            color = Color(0xFF123A73)
                                        )
                                        Text(
                                            text = "업무 ${preset.works.size}개",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6F84A2)
                                        )
                                    }

                                    RadioButton(
                                        selected = selectedPresetId == preset.id,
                                        onClick = { selectedPresetId = preset.id }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val newId = addViewModel.save(selectedPresetName)
                                if (newId != null) {
                                    Toast.makeText(context, "선박이 추가되었습니다", Toast.LENGTH_SHORT).show()
                                    onVesselSaved(newId)
                                }
                            },
                            enabled = vesselName.trim().isNotEmpty() && selectedPresetId != -1L
                        ) {
                            Text("저장")
                        }
                    }
                }
            }
        }
    }
}