package com.gomgom.eod.feature.task.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gomgom.eod.R
import com.gomgom.eod.feature.task.viewmodel.TaskPresetStateStore
import com.gomgom.eod.feature.task.viewmodel.TaskVesselAddViewModel

@Composable
fun TaskVesselAddScreen(
    onBackClick: () -> Unit,
    onGoPresetClick: () -> Unit,
    onVesselSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val addViewModel: TaskVesselAddViewModel = viewModel()
    val vesselName by addViewModel.vesselName.collectAsState()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()

    val activePresetName = presetGroups.firstOrNull { it.enabled }?.name.orEmpty()
    val hasActivePreset = activePresetName.isNotBlank()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_close),
                            tint = Color(0xFF123A73),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.task_top_add_vessel),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )
                }

                Box(modifier = Modifier.size(42.dp))
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
                TaskVesselAddForm(
                    vesselName = vesselName,
                    hasActivePreset = hasActivePreset,
                    onVesselNameChange = addViewModel::onVesselNameChange,
                    onGoPresetClick = onGoPresetClick,
                    onDismiss = onBackClick,
                    onSave = {
                        val newId = addViewModel.save(activePresetName)
                        if (newId != null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.task_vessel_add_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                            onVesselSaved(newId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskVesselAddDialog(
    onDismiss: () -> Unit,
    onGoPresetClick: () -> Unit,
    onVesselSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val addViewModel: TaskVesselAddViewModel = viewModel()
    val vesselName by addViewModel.vesselName.collectAsState()
    val presetGroups by TaskPresetStateStore.presetGroups.collectAsState()

    val activePresetName = presetGroups.firstOrNull { it.enabled }?.name.orEmpty()
    val hasActivePreset = activePresetName.isNotBlank()

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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            TaskVesselAddForm(
                vesselName = vesselName,
                hasActivePreset = hasActivePreset,
                onVesselNameChange = addViewModel::onVesselNameChange,
                onGoPresetClick = onGoPresetClick,
                onDismiss = onDismiss,
                onSave = {
                    val newId = addViewModel.save(activePresetName)
                    if (newId != null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.task_vessel_add_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        onVesselSaved(newId)
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskVesselAddForm(
    vesselName: String,
    hasActivePreset: Boolean,
    onVesselNameChange: (String) -> Unit,
    onGoPresetClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.task_top_add_vessel),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF123A73),
            textAlign = TextAlign.Center
        )

        if (!hasActivePreset) {
            Text(
                text = stringResource(R.string.task_vessel_add_no_active_preset),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                color = Color(0xFF5D7598),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onGoPresetClick) {
                    Text(
                        text = stringResource(R.string.task_vessel_add_go_preset),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        OutlinedTextField(
            value = vesselName,
            onValueChange = onVesselNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = taskOutlinedTextFieldColors(Color(0xFF123A73), Color(0xFF6E85A3)),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = stringResource(R.string.task_top_vessel_name_hint),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        )

        HorizontalDivider(color = Color(0xFFD6E0EC))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onSave,
                enabled = vesselName.trim().isNotEmpty() && hasActivePreset
            ) {
                Text(
                    text = stringResource(R.string.common_save),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
