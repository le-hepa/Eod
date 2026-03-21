package com.gomgom.eod.feature.portinfo.porttool.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.gomgom.eod.feature.portinfo.porttool.dao.PortAttachmentDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortConditionDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortLocationDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortOperationDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortRecordDao
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortConditionEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class PortToolRepositoryImpl(
    private val database: RoomDatabase,
    private val recordDao: PortRecordDao,
    private val operationDao: PortOperationDao,
    private val locationDao: PortLocationDao,
    private val conditionDao: PortConditionDao,
    private val attachmentDao: PortAttachmentDao
) : PortToolRepository {

    override fun observeRecords(): Flow<List<PortRecordEntity>> = recordDao.observeAll()

    override suspend fun getRecordBundle(recordId: Long): PortRecordBundle? = withContext(Dispatchers.IO) {
        val record = recordDao.findById(recordId) ?: return@withContext null
        PortRecordBundle(
            record = record,
            operations = operationDao.listByRecordId(recordId),
            locations = locationDao.listByRecordId(recordId),
            conditions = conditionDao.listByRecordId(recordId),
            attachments = attachmentDao.listByRecordId(recordId)
        )
    }

    override suspend fun searchCountryPort(country: String, port: String): List<PortRecordEntity> = withContext(Dispatchers.IO) {
        recordDao.searchCountryPort(country.normalizeSearch(), port.normalizeSearch())
    }

    override suspend fun searchAll(country: String, query: String): List<PortRecordEntity> = withContext(Dispatchers.IO) {
        recordDao.searchAll(country.normalizeSearch(), query.normalizeSearch())
    }

    override suspend fun searchByUnlocode(unlocode: String): List<PortRecordEntity> = withContext(Dispatchers.IO) {
        recordDao.searchByUnlocode(unlocode.trim())
    }

    override suspend fun saveRecordBundle(bundle: PortRecordBundle) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val record = bundle.record.withDerivedSearchFields().withContentHash()
        recordDao.upsert(record)
        operationDao.softDeleteByRecordId(record.id, now)
        locationDao.softDeleteByRecordId(record.id, now)
        conditionDao.softDeleteByRecordId(record.id, now)
        attachmentDao.softDeleteByRecordId(record.id, now)
        if (bundle.operations.isNotEmpty()) operationDao.upsertAll(bundle.operations)
        if (bundle.locations.isNotEmpty()) locationDao.upsertAll(bundle.locations)
        if (bundle.conditions.isNotEmpty()) conditionDao.upsertAll(bundle.conditions)
        if (bundle.attachments.isNotEmpty()) attachmentDao.upsertAll(bundle.attachments)
    }

    override suspend fun deleteRecord(recordId: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        operationDao.softDeleteByRecordId(recordId, now)
        locationDao.softDeleteByRecordId(recordId, now)
        conditionDao.softDeleteByRecordId(recordId, now)
        attachmentDao.softDeleteByRecordId(recordId, now)
        recordDao.softDeleteById(recordId, now)
    }

    override suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val records = recordDao.observeAll().first()
        val operations = mutableListOf<PortOperationEntity>()
        val locations = mutableListOf<PortLocationEntity>()
        val conditions = mutableListOf<PortConditionEntity>()
        val attachments = mutableListOf<PortAttachmentEntity>()
        records.forEach { record ->
            operations += operationDao.listByRecordId(record.id)
            locations += locationDao.listByRecordId(record.id)
            conditions += conditionDao.listByRecordId(record.id)
            attachments += attachmentDao.listByRecordId(record.id)
        }
        JSONObject()
            .put("toolType", PortToolType.PORT)
            .put("records", records.toJsonArray { it.toJson() })
            .put("operations", operations.toJsonArray { it.toJson() })
            .put("locations", locations.toJsonArray { it.toJson() })
            .put("conditions", conditions.toJsonArray { it.toJson() })
            .put("attachments", attachments.toJsonArray { it.toJson() })
            .toString()
    }

    override suspend fun exportCsv(): String = withContext(Dispatchers.IO) {
        val header = "id,country_code,country_name,port_name,unlocode,anchorage_name,berth_name,company,cargo_name,operator_name,safety_officer_name,surveyor_name,attachment_count,remark,created_at,updated_at"
        val rows = recordDao.observeAll().first().map {
            listOf(
                it.id.toString(),
                it.countryCode.csv(),
                it.countryName.csv(),
                it.portName.csv(),
                it.unlocode.csv(),
                it.anchorageName.csv(),
                it.berthName.csv(),
                it.company.csv(),
                it.cargoName.csv(),
                it.operatorName.csv(),
                it.safetyOfficerName.csv(),
                it.surveyorName.csv(),
                it.attachmentCount.toString(),
                it.generalRemark.csv(),
                it.createdAt.toString(),
                it.updatedAt.toString()
            ).joinToString(",")
        }
        buildString {
            appendLine(header)
            rows.forEach(::appendLine)
        }
    }

    override suspend fun importJson(json: String) = withContext(Dispatchers.IO) {
        val root = JSONObject(json)
        if (root.optString("toolType") != PortToolType.PORT) error("Port toolType mismatch")

        val records = root.optJSONArray("records").toRecordList()
        val operations = root.optJSONArray("operations").toOperationList()
        val locations = root.optJSONArray("locations").toLocationList()
        val conditions = root.optJSONArray("conditions").toConditionList()
        val attachments = root.optJSONArray("attachments").toAttachmentList()

        database.withTransaction {
            var nextRecordId = recordDao.maxId() + 1L
            var nextOperationId = operationDao.maxId() + 1L
            var nextLocationId = locationDao.maxId() + 1L
            var nextConditionId = conditionDao.maxId() + 1L
            var nextAttachmentId = attachmentDao.maxId() + 1L
            val locationIdMap = mutableMapOf<Long, Long>()

            records.forEach { imported ->
                val hashed = imported.withDerivedSearchFields().withContentHash()
                if (recordDao.findByContentHash(hashed.contentHash) != null) return@forEach

                val newRecordId = nextRecordId++
                recordDao.insert(hashed.copy(id = newRecordId))

                val mappedOperations = operations.filter { it.recordId == imported.id }.map {
                    it.copy(id = nextOperationId++, recordId = newRecordId)
                }
                if (mappedOperations.isNotEmpty()) operationDao.insertAll(mappedOperations)

                val mappedLocations = locations.filter { it.recordId == imported.id }.map {
                    val newId = nextLocationId++
                    locationIdMap[it.id] = newId
                    it.copy(id = newId, recordId = newRecordId)
                }
                if (mappedLocations.isNotEmpty()) locationDao.insertAll(mappedLocations)

                val mappedConditions = conditions.filter { it.recordId == imported.id }.map {
                    it.copy(id = nextConditionId++, recordId = newRecordId, locationId = it.locationId?.let(locationIdMap::get))
                }
                if (mappedConditions.isNotEmpty()) conditionDao.insertAll(mappedConditions)

                val mappedAttachments = attachments.filter { it.recordId == imported.id }.map {
                    it.copy(id = nextAttachmentId++, recordId = newRecordId)
                }
                if (mappedAttachments.isNotEmpty()) attachmentDao.insertAll(mappedAttachments)
            }
        }
    }
}

private fun PortRecordEntity.withContentHash(): PortRecordEntity = copy(contentHash = sha256(stablePortJson(this)))

private fun PortRecordEntity.withDerivedSearchFields(): PortRecordEntity {
    val countryPort = listOf(countryCode, countryName, portName, unlocode).joinToString(" ")
    val full = listOf(
        countryCode,
        countryName,
        portName,
        unlocode,
        berthName,
        company,
        anchorageName,
        supplyStatus,
        supplyRemark,
        wasteStatus,
        wasteRemark,
        crewChangeStatus,
        crewChangeRemark,
        cargoName,
        cargoRemark,
        manifoldConnectionType,
        manifoldStandard,
        manifoldSize,
        manifoldUnit,
        manifoldClass,
        manifoldRemark,
        transferRate,
        transferRateUnit,
        operatorName,
        safetyOfficerName,
        surveyorName,
        berthInfo,
        anchorageInfo,
        dischargeInfo,
        generalRemark,
        caution
    ).joinToString(" ")
    return copy(
        searchCountryPort = countryPort.normalizeSearch(),
        searchAll = full.normalizeSearch(),
        normalizedText = full.normalizeSearch()
    )
}

private fun stablePortJson(record: PortRecordEntity): String {
    val fields = sortedMapOf(
        "anchorageInfo" to normalizeForHash(record.anchorageInfo),
        "anchorageName" to normalizeForHash(record.anchorageName),
        "attachmentCount" to normalizeForHash(record.attachmentCount),
        "berthInfo" to normalizeForHash(record.berthInfo),
        "berthName" to normalizeForHash(record.berthName),
        "cargoRemark" to normalizeForHash(record.cargoRemark),
        "cargoName" to normalizeForHash(record.cargoName),
        "caution" to normalizeForHash(record.caution),
        "company" to normalizeForHash(record.company),
        "countryCode" to normalizeForHash(record.countryCode),
        "countryName" to normalizeForHash(record.countryName),
        "crewChangeRemark" to normalizeForHash(record.crewChangeRemark),
        "crewChangeStatus" to normalizeForHash(record.crewChangeStatus),
        "dischargeInfo" to normalizeForHash(record.dischargeInfo),
        "generalRemark" to normalizeForHash(record.generalRemark),
        "manifoldClass" to normalizeForHash(record.manifoldClass),
        "manifoldConnectionType" to normalizeForHash(record.manifoldConnectionType),
        "manifoldRemark" to normalizeForHash(record.manifoldRemark),
        "manifoldSize" to normalizeForHash(record.manifoldSize),
        "manifoldStandard" to normalizeForHash(record.manifoldStandard),
        "manifoldUnit" to normalizeForHash(record.manifoldUnit),
        "operatorName" to normalizeForHash(record.operatorName),
        "portName" to normalizeForHash(record.portName),
        "safetyOfficerName" to normalizeForHash(record.safetyOfficerName),
        "searchAll" to normalizeForHash(record.searchAll),
        "searchCountryPort" to normalizeForHash(record.searchCountryPort),
        "supplyRemark" to normalizeForHash(record.supplyRemark),
        "supplyStatus" to normalizeForHash(record.supplyStatus),
        "surveyorName" to normalizeForHash(record.surveyorName),
        "transferRate" to normalizeForHash(record.transferRate),
        "transferRateUnit" to normalizeForHash(record.transferRateUnit),
        "wasteRemark" to normalizeForHash(record.wasteRemark),
        "wasteStatus" to normalizeForHash(record.wasteStatus),
        "unlocode" to normalizeForHash(record.unlocode)
    )
    return fields.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":\"${escape(it.value)}\"" }
}

private fun normalizeForHash(value: String?): String = value ?: ""

private fun normalizeForHash(value: Int?): String = (value ?: 0).toString()

private fun normalizeForHash(value: Long?): String = (value ?: 0L).toString()

private fun normalizeForHash(value: Boolean?): String = (value ?: false).toString()

private fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

private fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
private fun String.normalizeSearch(): String = lowercase().replace(" ", "")

private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray = JSONArray().also { a -> forEach { a.put(transform(it)) } }
private fun String.csv(): String = "\"" + replace("\"", "\"\"") + "\""

private fun PortRecordEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("countryCode", countryCode).put("countryName", countryName).put("portName", portName)
    .put("unlocode", unlocode).put("anchorageName", anchorageName).put("berthName", berthName).put("company", company)
    .put("supplyStatus", supplyStatus).put("supplyRemark", supplyRemark)
    .put("wasteStatus", wasteStatus).put("wasteRemark", wasteRemark)
    .put("crewChangeStatus", crewChangeStatus).put("crewChangeRemark", crewChangeRemark)
    .put("cargoName", cargoName).put("cargoRemark", cargoRemark)
    .put("manifoldConnectionType", manifoldConnectionType).put("manifoldStandard", manifoldStandard)
    .put("manifoldSize", manifoldSize).put("manifoldUnit", manifoldUnit).put("manifoldClass", manifoldClass)
    .put("manifoldRemark", manifoldRemark).put("transferRate", transferRate).put("transferRateUnit", transferRateUnit)
    .put("operatorName", operatorName).put("safetyOfficerName", safetyOfficerName)
    .put("surveyorName", surveyorName).put("berthInfo", berthInfo).put("anchorageInfo", anchorageInfo)
    .put("caution", caution).put("dischargeInfo", dischargeInfo).put("generalRemark", generalRemark)
    .put("attachmentCount", attachmentCount).put("contentHash", contentHash).put("searchCountryPort", searchCountryPort)
    .put("searchAll", searchAll).put("normalizedText", normalizedText).put("isDeleted", isDeleted)
    .put("liveSearchEnabled", liveSearchEnabled).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun PortOperationEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("operationType", operationType).put("channelGroup", channelGroup)
    .put("channelValue", channelValue).put("remark", remark).put("normalizedText", normalizedText)
    .put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun PortLocationEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("locationType", locationType).put("name", name).put("info", info)
    .put("company", company).put("berthLengthMeters", berthLengthMeters).put("depthMeters", depthMeters)
    .put("berthingSide", berthingSide).put("mooringFore", mooringFore).put("mooringAft", mooringAft)
    .put("normalizedText", normalizedText).put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun PortConditionEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("locationId", locationId).put("side", side).put("cargoType", cargoType)
    .put("normalizedText", normalizedText).put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun PortAttachmentEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("attachmentType", attachmentType).put("displayName", displayName)
    .put("filePath", filePath).put("mimeType", mimeType).put("fileSize", fileSize).put("thumbnailPath", thumbnailPath)
    .put("normalizedText", normalizedText).put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun JSONArray?.toRecordList(): List<PortRecordEntity> = buildList {
    if (this@toRecordList == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        add(
            PortRecordEntity(
                id = item.getLong("id"),
                countryCode = item.optString("countryCode"),
                countryName = item.optString("countryName"),
                portName = item.optString("portName"),
                unlocode = item.optString("unlocode"),
                anchorageName = item.optString("anchorageName"),
                berthName = item.optString("berthName"),
                company = item.optString("company"),
                supplyStatus = item.optString("supplyStatus"),
                supplyRemark = item.optString("supplyRemark"),
                wasteStatus = item.optString("wasteStatus"),
                wasteRemark = item.optString("wasteRemark"),
                crewChangeStatus = item.optString("crewChangeStatus"),
                crewChangeRemark = item.optString("crewChangeRemark"),
                cargoName = item.optString("cargoName"),
                cargoRemark = item.optString("cargoRemark"),
                manifoldConnectionType = item.optString("manifoldConnectionType"),
                manifoldStandard = item.optString("manifoldStandard"),
                manifoldSize = item.optString("manifoldSize"),
                manifoldUnit = item.optString("manifoldUnit"),
                manifoldClass = item.optString("manifoldClass"),
                manifoldRemark = item.optString("manifoldRemark"),
                transferRate = item.optString("transferRate"),
                transferRateUnit = item.optString("transferRateUnit"),
                operatorName = item.optString("operatorName"),
                safetyOfficerName = item.optString("safetyOfficerName"),
                surveyorName = item.optString("surveyorName"),
                berthInfo = item.optString("berthInfo"),
                anchorageInfo = item.optString("anchorageInfo"),
                caution = item.optString("caution"),
                dischargeInfo = item.optString("dischargeInfo"),
                generalRemark = item.optString("generalRemark"),
                attachmentCount = item.optInt("attachmentCount"),
                contentHash = item.optString("contentHash"),
                searchCountryPort = item.optString("searchCountryPort"),
                searchAll = item.optString("searchAll"),
                normalizedText = item.optString("normalizedText"),
                isDeleted = item.optBoolean("isDeleted"),
                liveSearchEnabled = item.optBoolean("liveSearchEnabled"),
                createdAt = item.getLong("createdAt"),
                updatedAt = item.getLong("updatedAt")
            )
        )
    }
}

private fun JSONArray?.toOperationList(): List<PortOperationEntity> = buildList {
    if (this@toOperationList == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        add(PortOperationEntity(item.getLong("id"), item.getLong("recordId"), item.optString("operationType"), item.optString("channelGroup"), item.optString("channelValue"), item.optString("remark"), item.optString("normalizedText"), item.optBoolean("isDeleted"), item.getLong("createdAt"), item.getLong("updatedAt")))
    }
}

private fun JSONArray?.toLocationList(): List<PortLocationEntity> = buildList {
    if (this@toLocationList == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        add(PortLocationEntity(item.getLong("id"), item.getLong("recordId"), item.optString("locationType"), item.optString("name"), item.optString("info"), item.optString("company"), item.optString("berthLengthMeters"), item.optString("depthMeters"), item.optString("berthingSide"), item.optString("mooringFore"), item.optString("mooringAft"), item.optString("normalizedText"), item.optBoolean("isDeleted"), item.getLong("createdAt"), item.getLong("updatedAt")))
    }
}

private fun JSONArray?.toConditionList(): List<PortConditionEntity> = buildList {
    if (this@toConditionList == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        add(PortConditionEntity(item.getLong("id"), item.getLong("recordId"), item.optLong("locationId"), item.optString("side"), item.optString("cargoType"), item.optString("normalizedText"), item.optBoolean("isDeleted"), item.getLong("createdAt"), item.getLong("updatedAt")))
    }
}

private fun JSONArray?.toAttachmentList(): List<PortAttachmentEntity> = buildList {
    if (this@toAttachmentList == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        add(PortAttachmentEntity(item.getLong("id"), item.getLong("recordId"), item.optString("attachmentType"), item.optString("displayName"), item.optString("filePath"), item.optString("mimeType"), item.optLong("fileSize"), item.optString("thumbnailPath"), item.optString("normalizedText"), item.optBoolean("isDeleted"), item.getLong("createdAt"), item.getLong("updatedAt")))
    }
}
