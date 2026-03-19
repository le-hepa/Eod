package com.gomgom.eod

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.core.navigation.EodNavGraph
import com.gomgom.eod.ui.theme.EodTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLanguageManager.ensureDefaultLanguage(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EodTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val activity = LocalContext.current as? Activity
                    EodNavGraph(onExitApp = { activity?.finish() })
                }
            }
        }
    }
}