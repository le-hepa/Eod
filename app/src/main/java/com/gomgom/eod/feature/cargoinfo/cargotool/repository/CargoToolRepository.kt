package com.gomgom.eod.feature.cargoinfo.cargotool.repository

import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordBundle
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordEntity
import kotlinx.coroutines.flow.Flow

interface CargoToolRepository {
    fun observeRecords(): Flow<List<CargoRecordEntity>>
    suspend fun getRecordBundle(recordId: Long): CargoRecordBundle?
    suspend fun search(query: String): List<CargoRecordEntity>
    suspend fun saveRecordBundle(bundle: CargoRecordBundle)
    suspend fun deleteRecord(recordId: Long)
    suspend fun exportJson(): String
    suspend fun exportCsv(): String
    suspend fun importJson(json: String)
}
