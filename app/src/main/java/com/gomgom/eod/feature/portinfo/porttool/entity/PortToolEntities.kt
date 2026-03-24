package com.gomgom.eod.feature.portinfo.porttool.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "port_records",
    indices = [
        Index("isDeleted"),
        Index("countryCode"),
        Index("countryName"),
        Index("portName"),
        Index("berthName"),
        Index("company"),
        Index("cargoName"),
        Index("unlocode"),
        Index("updatedAt"),
        Index("contentHash"),
        Index("searchCountryPort"),
        Index("searchAll"),
        Index("normalizedText")
    ]
)
data class PortRecordEntity(
    @PrimaryKey val id: Long,
    val countryCode: String = "",
    val countryName: String = "",
    val portName: String = "",
    val unlocode: String = "",
    val anchorageName: String = "",
    val berthName: String = "",
    val company: String = "",
    val supplyStatus: String = "",
    val freshWaterStatus: String = "",
    val storeSpareStatus: String = "",
    val provisionsStatus: String = "",
    val supplyRemark: String = "",
    val wasteStatus: String = "",
    val slopStatus: String = "",
    val wasteRemark: String = "",
    val crewChangeStatus: String = "",
    val externalAuditStatus: String = "",
    val crewChangeRemark: String = "",
    val cargoName: String = "",
    val cargoRemark: String = "",
    val manifoldConnectionType: String = "",
    val manifoldStandard: String = "",
    val manifoldSize: String = "",
    val manifoldUnit: String = "",
    val manifoldClass: String = "",
    val manifoldRemark: String = "",
    val transferRate: String = "",
    val transferRateUnit: String = "",
    val operatorName: String = "",
    val safetyOfficerName: String = "",
    val surveyorName: String = "",
    val berthInfo: String = "",
    val anchorageInfo: String = "",
    val caution: String = "",
    val dischargeInfo: String = "",
    val generalRemark: String = "",
    val attachmentCount: Int = 0,
    val contentHash: String = "",
    val searchCountryPort: String = "",
    val searchAll: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val liveSearchEnabled: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "port_operations", indices = [Index("recordId"), Index("operationType"), Index("updatedAt"), Index("isDeleted")])
data class PortOperationEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val operationType: String,
    val channelGroup: String = "",
    val channelValue: String = "",
    val arrivalDate: String = "",
    val remark: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "port_locations", indices = [Index("recordId"), Index("locationType"), Index("name"), Index("company"), Index("updatedAt"), Index("isDeleted"), Index("normalizedText")])
data class PortLocationEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val locationType: String,
    val name: String = "",
    val info: String = "",
    val company: String = "",
    val berthLengthMeters: String = "",
    val depthMeters: String = "",
    val berthingSide: String = "",
    val mooringFore: String = "",
    val mooringAft: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "port_conditions", indices = [Index("recordId"), Index("locationId"), Index("side"), Index("cargoType"), Index("updatedAt"), Index("isDeleted"), Index("normalizedText")])
data class PortConditionEntity(
    @PrimaryKey val id: Long,
    val recordId: Long,
    val locationId: Long?,
    val side: String = "",
    val cargoType: String = "",
    val normalizedText: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "port_attachments", indices = [Index("recordId"), Index("attachmentType"), Index("updatedAt"), Index("isDeleted"), Index("normalizedText")])
data class PortAttachmentEntity(
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

data class PortRecordBundle(
    val record: PortRecordEntity,
    val operations: List<PortOperationEntity> = emptyList(),
    val locations: List<PortLocationEntity> = emptyList(),
    val conditions: List<PortConditionEntity> = emptyList(),
    val attachments: List<PortAttachmentEntity> = emptyList()
)

object PortToolCategory {
    const val VESSEL_REPORTING = "VESSEL_REPORTING"
    const val ANCHORAGE = "ANCHORAGE"
    const val BERTH = "BERTH"
    const val CARGO = "CARGO"
    const val MANIFOLD = "MANIFOLD"
    const val DISCHARGE = "DISCHARGE"
    const val WORKER = "WORKER"
    const val SAFETY = "SAFETY"
    const val EXTRA = "EXTRA"
}

object PortToolType {
    const val WORK_PRESET = "WORK_PRESET"
    const val PORT = "PORT"
    const val CARGO = "CARGO"
    const val VTS = "VTS"
    const val PILOT = "PILOT"
    const val TUG = "TUG"
    const val CIQ = "CIQ"
    const val AGENT = "AGENT"
    const val ANCHORAGE = "ANCHORAGE"
    const val BERTH = "BERTH"
    const val IMAGE = "IMAGE"
    const val VIDEO = "VIDEO"
    const val FILE = "FILE"
}
