package com.gomgom.eod.feature.cargoinfo.cargotool.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cargo_records",
    indices = [
        Index("countryCode"),
        Index("portName"),
        Index("berthName"),
        Index("company"),
        Index("cargoName"),
        Index("updatedAt"),
        Index("isDeleted"),
        Index("contentHash"),
        Index("searchText"),
        Index("normalizedText")
    ]
)
data class CargoRecordEntity(
    @PrimaryKey val id: Long,
    val countryCode: String = "",
    val countryName: String = "",
    val portName: String = "",
    val unlocode: String = "",
    val cargoName: String = "",
    val berthName: String = "",
    val company: String = "",
    val workerName: String = "",
    val safetyOfficerName: String = "",
    val surveyorName: String = "",
    val remark: String = "",
    val attachmentCount: Int = 0,
    val contentHash: String = "",
    val searchText: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "cargo_conditions", indices = [Index("recordId"), Index("category"), Index("value"), Index("updatedAt"), Index("isDeleted"), Index("normalizedText")])
data class CargoConditionEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val category: String,
    val value: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "cargo_attachments", indices = [Index("recordId"), Index("attachmentType"), Index("updatedAt"), Index("isDeleted"), Index("normalizedText")])
data class CargoAttachmentEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val attachmentType: String,
    val displayName: String = "",
    val filePath: String = "",
    val mimeType: String = "",
    val fileSize: Long = 0L,
    val thumbnailPath: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

data class CargoRecordBundle(
    val record: CargoRecordEntity,
    val conditions: List<CargoConditionEntity> = emptyList(),
    val attachments: List<CargoAttachmentEntity> = emptyList()
)
