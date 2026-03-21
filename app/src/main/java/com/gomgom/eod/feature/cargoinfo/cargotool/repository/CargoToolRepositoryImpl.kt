package com.gomgom.eod.feature.cargoinfo.cargotool.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoAttachmentDao
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoConditionDao
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoRecordDao
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoAttachmentEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoConditionEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordBundle
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class CargoToolRepositoryImpl(
    private val database: RoomDatabase,
    private val recordDao: CargoRecordDao,
    private val conditionDao: CargoConditionDao,
    private val attachmentDao: CargoAttachmentDao
) : CargoToolRepository {

    override fun observeRecords(): Flow<List<CargoRecordEntity>> = recordDao.observeAll()

    override suspend fun getRecordBundle(recordId: Long): CargoRecordBundle? = withContext(Dispatchers.IO) {
        val record = recordDao.findById(recordId) ?: return@withContext null
        CargoRecordBundle(
            record = record,
            conditions = conditionDao.listByRecordId(recordId),
            attachments = attachmentDao.listByRecordId(recordId)
        )
    }

    override suspend fun search(query: String): List<CargoRecordEntity> = withContext(Dispatchers.IO) {
        recordDao.search(query.trim())
    }

    override suspend fun saveRecordBundle(bundle: CargoRecordBundle) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val record = bundle.record.withContentHash()
        recordDao.upsert(record)
        conditionDao.softDeleteByRecordId(record.id, now)
        attachmentDao.softDeleteByRecordId(record.id, now)
        if (bundle.conditions.isNotEmpty()) conditionDao.upsertAll(bundle.conditions)
        if (bundle.attachments.isNotEmpty()) attachmentDao.upsertAll(bundle.attachments)
    }

    override suspend fun deleteRecord(recordId: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        conditionDao.softDeleteByRecordId(recordId, now)
        attachmentDao.softDeleteByRecordId(recordId, now)
        recordDao.softDeleteById(recordId, now)
    }

    override suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val records = recordDao.observeAll().first()
        val conditions = mutableListOf<CargoConditionEntity>()
        val attachments = mutableListOf<CargoAttachmentEntity>()
        records.forEach { record ->
            conditions += conditionDao.listByRecordId(record.id)
            attachments += attachmentDao.listByRecordId(record.id)
        }
        JSONObject()
            .put("toolType", PortToolType.CARGO)
            .put("records", JSONArray().apply { records.forEach { put(it.toJson()) } })
            .put("conditions", JSONArray().apply { conditions.forEach { put(it.toJson()) } })
            .put("attachments", JSONArray().apply { attachments.forEach { put(it.toJson()) } })
            .toString()
    }

    override suspend fun exportCsv(): String = withContext(Dispatchers.IO) {
        val header = "id,country_code,country_name,port_name,unlocode,cargo_name,berth_name,company,worker_name,safety_officer_name,surveyor_name,attachment_count,remark,created_at,updated_at"
        val rows = recordDao.observeAll().first().map {
            listOf(
                it.id.toString(),
                it.countryCode.csv(),
                it.countryName.csv(),
                it.portName.csv(),
                it.unlocode.csv(),
                it.cargoName.csv(),
                it.berthName.csv(),
                it.company.csv(),
                it.workerName.csv(),
                it.safetyOfficerName.csv(),
                it.surveyorName.csv(),
                it.attachmentCount.toString(),
                it.remark.csv(),
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
        if (root.optString("toolType") != PortToolType.CARGO) error("Cargo toolType mismatch")

        val recordArray = root.optJSONArray("records") ?: JSONArray()
        val conditionArray = root.optJSONArray("conditions")
        val attachmentArray = root.optJSONArray("attachments")

        database.withTransaction {
            var nextRecordId = recordDao.maxId() + 1L
            var nextConditionId = conditionDao.maxId() + 1L
            var nextAttachmentId = attachmentDao.maxId() + 1L

            for (index in 0 until recordArray.length()) {
                val item = recordArray.getJSONObject(index)
                val record = CargoRecordEntity(
                    id = item.getLong("id"),
                    countryCode = item.optString("countryCode"),
                    countryName = item.optString("countryName"),
                    portName = item.optString("portName"),
                    unlocode = item.optString("unlocode"),
                    cargoName = item.optString("cargoName"),
                    berthName = item.optString("berthName"),
                    company = item.optString("company"),
                    workerName = item.optString("workerName"),
                    safetyOfficerName = item.optString("safetyOfficerName"),
                    surveyorName = item.optString("surveyorName"),
                    remark = item.optString("remark"),
                    attachmentCount = item.optInt("attachmentCount"),
                    contentHash = item.optString("contentHash"),
                    searchText = item.optString("searchText"),
                    normalizedText = item.optString("normalizedText"),
                    isDeleted = item.optBoolean("isDeleted"),
                    createdAt = item.getLong("createdAt"),
                    updatedAt = item.getLong("updatedAt")
                ).withContentHash()

                if (recordDao.findByContentHash(record.contentHash) != null) continue

                val newRecordId = nextRecordId++
                recordDao.insert(record.copy(id = newRecordId))

                val conditions = conditionArray.toConditions(record.id).map {
                    it.copy(id = nextConditionId++, recordId = newRecordId)
                }
                if (conditions.isNotEmpty()) conditionDao.insertAll(conditions)

                val attachments = attachmentArray.toAttachments(record.id).map {
                    it.copy(id = nextAttachmentId++, recordId = newRecordId)
                }
                if (attachments.isNotEmpty()) attachmentDao.insertAll(attachments)
            }
        }
    }
}

private fun CargoRecordEntity.withContentHash(): CargoRecordEntity = copy(contentHash = sha256(stableCargoJson(this)))

private fun stableCargoJson(record: CargoRecordEntity): String {
    val fields = sortedMapOf(
        "attachmentCount" to normalizeForHash(record.attachmentCount),
        "berthName" to normalizeForHash(record.berthName),
        "cargoName" to normalizeForHash(record.cargoName),
        "company" to normalizeForHash(record.company),
        "countryCode" to normalizeForHash(record.countryCode),
        "countryName" to normalizeForHash(record.countryName),
        "portName" to normalizeForHash(record.portName),
        "remark" to normalizeForHash(record.remark),
        "safetyOfficerName" to normalizeForHash(record.safetyOfficerName),
        "searchText" to normalizeForHash(record.searchText),
        "surveyorName" to normalizeForHash(record.surveyorName),
        "unlocode" to normalizeForHash(record.unlocode),
        "workerName" to normalizeForHash(record.workerName)
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

private fun CargoRecordEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("countryCode", countryCode).put("countryName", countryName).put("portName", portName)
    .put("unlocode", unlocode).put("cargoName", cargoName).put("berthName", berthName).put("company", company)
    .put("workerName", workerName).put("safetyOfficerName", safetyOfficerName).put("surveyorName", surveyorName)
    .put("remark", remark).put("attachmentCount", attachmentCount).put("contentHash", contentHash)
    .put("searchText", searchText).put("normalizedText", normalizedText).put("isDeleted", isDeleted)
    .put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun CargoConditionEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("category", category).put("value", value)
    .put("normalizedText", normalizedText).put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun CargoAttachmentEntity.toJson(): JSONObject = JSONObject()
    .put("id", id).put("recordId", recordId).put("attachmentType", attachmentType).put("displayName", displayName)
    .put("filePath", filePath).put("mimeType", mimeType).put("fileSize", fileSize).put("thumbnailPath", thumbnailPath)
    .put("normalizedText", normalizedText).put("isDeleted", isDeleted).put("createdAt", createdAt).put("updatedAt", updatedAt)

private fun String.csv(): String = "\"" + replace("\"", "\"\"") + "\""

private fun JSONArray?.toConditions(recordId: Long): List<CargoConditionEntity> = buildList {
    if (this@toConditions == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        if (item.getLong("recordId") == recordId) {
            add(
                CargoConditionEntity(
                    id = item.getLong("id"),
                    recordId = recordId,
                    category = item.optString("category"),
                    value = item.optString("value"),
                    normalizedText = item.optString("normalizedText"),
                    isDeleted = item.optBoolean("isDeleted"),
                    createdAt = item.getLong("createdAt"),
                    updatedAt = item.getLong("updatedAt")
                )
            )
        }
    }
}

private fun JSONArray?.toAttachments(recordId: Long): List<CargoAttachmentEntity> = buildList {
    if (this@toAttachments == null) return@buildList
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        if (item.getLong("recordId") == recordId) {
            add(
                CargoAttachmentEntity(
                    id = item.getLong("id"),
                    recordId = recordId,
                    attachmentType = item.optString("attachmentType"),
                    displayName = item.optString("displayName"),
                    filePath = item.optString("filePath"),
                    mimeType = item.optString("mimeType"),
                    fileSize = item.optLong("fileSize"),
                    thumbnailPath = item.optString("thumbnailPath"),
                    normalizedText = item.optString("normalizedText"),
                    isDeleted = item.optBoolean("isDeleted"),
                    createdAt = item.getLong("createdAt"),
                    updatedAt = item.getLong("updatedAt")
                )
            )
        }
    }
}
