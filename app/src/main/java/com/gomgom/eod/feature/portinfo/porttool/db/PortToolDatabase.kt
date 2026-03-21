package com.gomgom.eod.feature.portinfo.porttool.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gomgom.eod.feature.portinfo.porttool.dao.PortAttachmentDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortConditionDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortLocationDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortOperationDao
import com.gomgom.eod.feature.portinfo.porttool.dao.PortRecordDao
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortConditionEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity

@Database(
    entities = [
        PortRecordEntity::class,
        PortOperationEntity::class,
        PortLocationEntity::class,
        PortConditionEntity::class,
        PortAttachmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PortLocalDatabase : RoomDatabase() {
    abstract fun recordDao(): PortRecordDao
    abstract fun operationDao(): PortOperationDao
    abstract fun locationDao(): PortLocationDao
    abstract fun conditionDao(): PortConditionDao
    abstract fun attachmentDao(): PortAttachmentDao
}

@Database(
    entities = [
        PortRecordEntity::class,
        PortOperationEntity::class,
        PortLocationEntity::class,
        PortConditionEntity::class,
        PortAttachmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PortSharedDatabase : RoomDatabase() {
    abstract fun recordDao(): PortRecordDao
    abstract fun operationDao(): PortOperationDao
    abstract fun locationDao(): PortLocationDao
    abstract fun conditionDao(): PortConditionDao
    abstract fun attachmentDao(): PortAttachmentDao
}

object PortLocalDatabaseProvider {
    @Volatile
    private var instance: PortLocalDatabase? = null

    fun get(context: Context): PortLocalDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PortLocalDatabase::class.java,
                "eod_port_local.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}

object PortSharedDatabaseProvider {
    @Volatile
    private var instance: PortSharedDatabase? = null

    fun get(context: Context): PortSharedDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PortSharedDatabase::class.java,
                "eod_port_shared.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
