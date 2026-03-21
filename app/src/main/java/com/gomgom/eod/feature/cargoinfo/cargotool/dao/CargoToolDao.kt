package com.gomgom.eod.feature.cargoinfo.cargotool.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoAttachmentEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoConditionEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CargoRecordDao {
    @Query("SELECT * FROM cargo_records WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CargoRecordEntity>>

    @Query("SELECT * FROM cargo_records WHERE id = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun findById(recordId: Long): CargoRecordEntity?

    @Query("SELECT * FROM cargo_records WHERE isDeleted = 0 AND searchText LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun search(query: String): List<CargoRecordEntity>

    @Query("SELECT * FROM cargo_records WHERE isDeleted = 0 AND contentHash = :contentHash ORDER BY updatedAt DESC LIMIT 1")
    suspend fun findByContentHash(contentHash: String): CargoRecordEntity?

    @Query("SELECT COALESCE(MAX(id), 0) FROM cargo_records")
    suspend fun maxId(): Long

    @Query("SELECT * FROM cargo_records WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun listPaged(limit: Int, offset: Int): List<CargoRecordEntity>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insert(record: CargoRecordEntity)

    @Upsert
    suspend fun upsert(record: CargoRecordEntity)

    @Upsert
    suspend fun upsertAll(records: List<CargoRecordEntity>)

    @Query("UPDATE cargo_records SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :recordId")
    suspend fun softDeleteById(recordId: Long, updatedAt: Long)
}

@Dao
interface CargoConditionDao {
    @Query("SELECT * FROM cargo_conditions WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<CargoConditionEntity>

    @Upsert
    suspend fun upsertAll(items: List<CargoConditionEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM cargo_conditions")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<CargoConditionEntity>)

    @Query("UPDATE cargo_conditions SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}

@Dao
interface CargoAttachmentDao {
    @Query("SELECT * FROM cargo_attachments WHERE recordId = :recordId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun listByRecordId(recordId: Long): List<CargoAttachmentEntity>

    @Upsert
    suspend fun upsertAll(items: List<CargoAttachmentEntity>)

    @Query("SELECT COALESCE(MAX(id), 0) FROM cargo_attachments")
    suspend fun maxId(): Long

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<CargoAttachmentEntity>)

    @Query("UPDATE cargo_attachments SET isDeleted = 1, updatedAt = :updatedAt WHERE recordId = :recordId")
    suspend fun softDeleteByRecordId(recordId: Long, updatedAt: Long)
}
