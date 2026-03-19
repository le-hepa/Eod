package com.gomgom.eod.feature.home.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeContactScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val template = """
        EoD 문의
        - 기기: ${Build.MANUFACTURER} ${Build.MODEL}
        - Android: ${Build.VERSION.RELEASE}
        - 문의 내용:
    """.trimIndent()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8FC))
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "문의",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF123A73)
                    )

                    Text(
                        text = "문의 템플릿을 복사한 뒤 메일이나 메모에 붙여넣어 사용합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF123A73)
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("eod_contact_template", template)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                "문의 템플릿이 복사되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text("문의 템플릿 복사")
                    }

                    Text(
                        text = template,
                        fontSize = 14.sp,
                        color = Color(0xFF123A73)
                    )
                }
            }
        }
    }
}