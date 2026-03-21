package com.gomgom.eod.feature.portinfo.porttool.repository

import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortConditionEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import kotlinx.coroutines.flow.Flow

interface PortToolRepository {
    fun observeRecords(): Flow<List<PortRecordEntity>>
    suspend fun getRecordBundle(recordId: Long): PortRecordBundle?
    suspend fun searchCountryPort(country: String, port: String): List<PortRecordEntity>
    suspend fun searchAll(country: String, query: String): List<PortRecordEntity>
    suspend fun searchByUnlocode(unlocode: String): List<PortRecordEntity>
    suspend fun saveRecordBundle(bundle: PortRecordBundle)
    suspend fun deleteRecord(recordId: Long)
    suspend fun exportJson(): String
    suspend fun exportCsv(): String
    suspend fun importJson(json: String)
}

data class PortEditorState(
    val record: PortRecordEntity,
    val operations: List<PortOperationEntity> = emptyList(),
    val locations: List<PortLocationEntity> = emptyList(),
    val conditions: List<PortConditionEntity> = emptyList(),
    val attachments: List<PortAttachmentEntity> = emptyList()
)
