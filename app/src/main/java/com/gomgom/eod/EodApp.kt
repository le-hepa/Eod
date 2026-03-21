package com.gomgom.eod

import android.app.Application
import android.content.Context
import com.gomgom.eod.feature.task.db.TaskDatabaseProvider
import com.gomgom.eod.feature.task.db.TaskRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EodApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        appScope.launch {
            TaskRoomDatabase.getInstance(applicationContext)
            TaskRoomDatabase.ensureMigrated(applicationContext)
            TaskDatabaseProvider.database
        }
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
