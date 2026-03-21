package com.gomgom.eod.feature.portinfo.porttool.wrapper

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gomgom.eod.feature.portinfo.porttool.db.PortLocalDatabaseProvider
import com.gomgom.eod.feature.portinfo.porttool.db.PortSharedDatabaseProvider
import com.gomgom.eod.feature.portinfo.porttool.repository.PortToolRepository
import com.gomgom.eod.feature.portinfo.porttool.repository.PortToolRepositoryImpl
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeLookupRepository
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolSource
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortToolViewModel

object PortToolSession {
    var activeSource: PortToolSource by mutableStateOf(PortToolSource.LOCAL)
}

fun createPortToolRepository(
    context: Context,
    source: PortToolSource
): PortToolRepository {
    return if (source == PortToolSource.LOCAL) {
        val database = PortLocalDatabaseProvider.get(context)
        PortToolRepositoryImpl(
            database = database,
            recordDao = database.recordDao(),
            operationDao = database.operationDao(),
            locationDao = database.locationDao(),
            conditionDao = database.conditionDao(),
            attachmentDao = database.attachmentDao()
        )
    } else {
        val database = PortSharedDatabaseProvider.get(context)
        PortToolRepositoryImpl(
            database = database,
            recordDao = database.recordDao(),
            operationDao = database.operationDao(),
            locationDao = database.locationDao(),
            conditionDao = database.conditionDao(),
            attachmentDao = database.attachmentDao()
        )
    }
}

fun portToolViewModelFactory(
    repository: PortToolRepository,
    lookupRepository: PortUnlocodeLookupRepository
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PortToolViewModel(repository, lookupRepository) as T
    }
}
