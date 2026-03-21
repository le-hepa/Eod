package com.gomgom.eod.feature.portinfo.porttool.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortConditionEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortRecordDao {
    @Query("SELECT * FROM port_records WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PortRecordEntity>>

    @Query("SELECT * FROM port_records WHERE id = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun findById(recordId: Long): PortRecordEntity?

    @Query(
        "SELECT * FROM port_records " +
            "WHERE isDeleted = 0 " +
            "AND (:countryQuery = '' OR searchCountryPort LIKE '%' || :countryQuery || '%') " +
            "AND (:portQuery = '' OR searchCountryPort LIKE '%' || :portQuery || '%') " +
            "ORDER BY updatedAt DESC"
    )
    suspend fun searchCountryPort(countryQuery: String, portQuery: String): List<PortRecordEntity>

    @Query(
        "SELECT * FROM port_records " +
            "WHERE isDeleted = 0 " +
            "AND (:countryQuery = '' OR searchCountryPort LIKE '%' || :countryQuery || '%') " +
            "AND (:query = '' OR normalizedText LIKE '%' || :query || '%') " +
            "ORDER BY updatedAt DESC"
    )
    suspend fun searchAll(countryQuery: String, query: String): List<PortRecordEntity>

    @Query("SELECT * FROM port_records WHERE isDeleted = 0 AND unlocode = :unlocode ORDER BY updatedAt DESC")
    suspend fun searchByUnlocode(unlocode: String): List<PortRecordEntity>

    @Query("SELECT * FROM port_records WHERE isDeleted = 0 AND contentHash = :contentHash ORDER BY updatedAt DESC LIMIT 1")
    suspend fun findByContentHash(contentHash: String): PortRecordEntity?

    @Query("SELECT COALESCE(MAX(id), 0) FROM port_records")
    suspend fun maxId(): Long

    @Query("SELECT * FROM port_records WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun listPaged(limit: Int, offset: Int): List<PortRecordEntity>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insert(record: PortRecordEntity)

    @Upsert
    suspend fun upsert(record: PortRecordEntity)

    @Upsert
    suspend fun upsertAll(records: List<PortRecordEntity>)

    @Delete
    suspend fun delete(record: PortRecordEntity)

    @Query("UPDATE port_records SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :recordId")
    suspend fun softDeleteById(recordId: Long, updatedAt: Long)
}

@Dao
interface PortOperationDao {
    @Query("SELECT * FROM port_operations WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<PortOperationEntity>

    @Upsert
    suspend fun upsertAll(items: List<PortOperationEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM port_operations")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PortOperationEntity>)

    @Query("UPDATE port_operations SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}

@Dao
interface PortLocationDao {
    @Query("SELECT * FROM port_locations WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<PortLocationEntity>

    @Upsert
    suspend fun upsertAll(items: List<PortLocationEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM port_locations")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PortLocationEntity>)

    @Query("UPDATE port_locations SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}

@Dao
interface PortConditionDao {
    @Query("SELECT * FROM port_conditions WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<PortConditionEntity>

    @Upsert
    suspend fun upsertAll(items: List<PortConditionEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM port_conditions")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PortConditionEntity>)

    @Query("UPDATE port_conditions SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}

@Dao
interface PortAttachmentDao {
    @Query("SELECT * FROM port_attachments WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<PortAttachmentEntity>

    @Upsert
    suspend fun upsertAll(items: List<PortAttachmentEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM port_attachments")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PortAttachmentEntity>)

    @Query("UPDATE port_attachments SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}
