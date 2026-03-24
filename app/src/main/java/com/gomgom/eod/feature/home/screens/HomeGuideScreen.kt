package com.gomgom.eod.feature.home.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomgom.eod.R

private data class GuideItem(
    val title: String,
    val body: String
)

@Composable
fun HomeGuideScreen(
    onBackClick: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }

    val guideItems = listOf(
        GuideItem(
            title = stringResource(R.string.task_guide_feature_home_title),
            body = stringResource(R.string.task_guide_feature_home_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_task_main_title),
            body = stringResource(R.string.task_guide_feature_task_main_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_preset_title),
            body = stringResource(R.string.task_guide_feature_preset_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_regular_work_title),
            body = stringResource(R.string.task_guide_feature_regular_work_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_alarm_list_title),
            body = stringResource(R.string.task_guide_feature_alarm_list_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_record_home_title),
            body = stringResource(R.string.task_guide_feature_record_home_body)
        ),
        GuideItem(
            title = stringResource(R.string.task_guide_feature_record_write_title),
            body = stringResource(R.string.task_guide_feature_record_write_body)
        )
    )

    val filteredItems = if (keyword.isBlank()) {
        guideItems
    } else {
        guideItems.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                it.body.contains(keyword, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFFF5F8FC),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.home_guide_close),
                        tint = Color(0xFF123A73)
                    )
                }
                Text(
                    text = stringResource(R.string.home_guide_title),
                    color = Color(0xFF123A73),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
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
                    label = { Text(stringResource(R.string.home_guide_search_hint)) }
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
                            text = stringResource(R.string.home_guide_no_result),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            fontSize = 16.sp,
                            color = Color(0xFF123A73),
                            textAlign = TextAlign.Center
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
                        Column(
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
