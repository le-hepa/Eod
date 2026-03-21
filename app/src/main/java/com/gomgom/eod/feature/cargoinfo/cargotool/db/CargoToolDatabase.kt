package com.gomgom.eod.feature.cargoinfo.cargotool.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoAttachmentDao
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoConditionDao
import com.gomgom.eod.feature.cargoinfo.cargotool.dao.CargoRecordDao
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoAttachmentEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoConditionEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordEntity

@Database(
    entities = [
        CargoRecordEntity::class,
        CargoConditionEntity::class,
        CargoAttachmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CargoLocalDatabase : RoomDatabase() {
    abstract fun recordDao(): CargoRecordDao
    abstract fun conditionDao(): CargoConditionDao
    abstract fun attachmentDao(): CargoAttachmentDao
}

@Database(
    entities = [
        CargoRecordEntity::class,
        CargoConditionEntity::class,
        CargoAttachmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CargoSharedDatabase : RoomDatabase() {
    abstract fun recordDao(): CargoRecordDao
    abstract fun conditionDao(): CargoConditionDao
    abstract fun attachmentDao(): CargoAttachmentDao
}

object CargoLocalDatabaseProvider {
    @Volatile
    private var instance: CargoLocalDatabase? = null

    fun get(context: Context): CargoLocalDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CargoLocalDatabase::class.java,
                "eod_cargo_local.db"
            ).build().also { instance = it }
        }
    }
}

object CargoSharedDatabaseProvider {
    @Volatile
    private var instance: CargoSharedDatabase? = null

    fun get(context: Context): CargoSharedDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CargoSharedDatabase::class.java,
                "eod_cargo_shared.db"
            ).build().also { instance = it }
        }
    }
}
