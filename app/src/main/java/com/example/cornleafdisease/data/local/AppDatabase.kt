package com.example.cornleafdisease.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanResultEntity::class,
        DiseaseEntity::class,
        ImageEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scanResultDao(): ScanResultDao
    abstract fun diseaseDao(): DiseaseDao
    abstract fun imageDao(): ImageDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "corn_leaf_database_v3"
                ).build().also { INSTANCE = it }
            }
        }
    }
}